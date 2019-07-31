var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');

var envalid = require('envalid')
var jwt = require('jsonwebtoken');
var axios = require('axios')
var crypto = require("crypto");

const jwtSecret = 'shhhhh';

var sessionStore = {};

const { str, url } = envalid
 
const env = envalid.cleanEnv(process.env, {
    TOKEN_HOST:                url(),
    TOKEN_PATH:                str(),
    AUTHORIZE_PATH:            str(),
    USERINFO_URL:              url(),
    MATTERMOST_HOST:           url(),
    MATTERMOST_ADMIN_USER:     str(),
    MATTERMOST_ADMIN_PASSWORD: str(),
    MATTERMOST_APP_NAME:       str()
});

var credentials = {}
var oauth2;

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.get('/health', function(req, res, next) {
  return res.send('OK')
})

app.get('/validate', function(req, res, next) {
  console.log("Validating")

  var token = req.cookies.codeserver;
  var payload;

  if (token !== undefined) {
    console.log("Found cookie jwt")

    try {
      var cookiePayload = jwt.verify(token, jwtSecret);

      var id = cookiePayload.id;

      if(sessionStore[id] !== undefined) {
        console.log("Found session in store, hydrating")

        payload = sessionStore[id];
      }
    }
    catch(e) {
      console.log("Failed to verify JWT in validate")
    }
  } 

  if(payload === undefined) {
    console.log("Payload is undefined, generating session")

    const id = crypto.randomBytes(16).toString("hex");

    payload = {
      id: id
    }

    sessionStore[id] = payload;
  }

  res.set('X-OAuth-Error', '');

  if (payload.profile === undefined) {
    console.log("Profile not present, unauthorized")
    res.status(401);
  }
  else {
    console.log("Profile is present")

    if(req.query.users !== undefined) {
      var users = req.query.users
      console.log("Got users header")

      if(payload.profile.username != users) {
        console.log("Profile username "+payload.profile.username+" does not match header "+users)
        res.set('X-OAuth-Error', 'unauthorized');
        res.status(401);
      }
    }
  }

  var newToken = jwt.sign(payload, jwtSecret);

  res.cookie('codeserver', newToken, { 
    maxAge: 900000,
    httpOnly: true,
  });

  res.set('X-OAuth-Jwt', newToken);

  return res.send("Validated")
})

app.get('/login', function(req, res, next) {
  var rd = req.query.rd
  var token = req.query.token;
  var err = req.query.err;

  if(err !== undefined) {
    if (err == 'unauthorized') {
      res.status(401)
      return res.send("Unauthorized")
    }
  }
   
  var state = jwt.sign({
    rd: rd,
    token: token,
  }, jwtSecret);
  
  const authorizationUri = oauth2.authorizationCode.authorizeURL({
    redirect_uri: 'https://'+req.get('host')+'/oauth/callback',
    state: state
  });
  
  res.redirect(authorizationUri);
});

app.get('/oauth/callback', async (req, res, next) => {
  const code = req.query.code;
  const stateParam = req.query.state;

  try {
    var state = jwt.verify(stateParam, jwtSecret);
    var token = jwt.verify(state.token, jwtSecret)
  }
  catch(e) {
    console.error("Problem parsing JWT tokens: "+e)

    res.status(500)
    return res.end()
  }

  const tokenOptions = {
    code: code,
    redirect_uri: 'https://'+req.get('host')+'/oauth/callback',
  };

  try {
    console.log(tokenOptions);

    const result = await oauth2.authorizationCode.getToken(tokenOptions);

    oauth2.accessToken.create(result);

    const response = await axios.get(env.USERINFO_URL, {
      headers: {
        "Authorization" : "Bearer "+result.access_token
      }
    });

    token.profile = response.data;

    var jwtToken = jwt.sign(token, jwtSecret);
    res.cookie('codeserver', jwtToken, { 
      maxAge: 900000,
      httpOnly: true,
    });

    sessionStore[token.id] = token;

    return res.redirect(302, state.rd)
  } catch(error) {
    console.error('Callback error', error);
    return res.status(500).json('Authentication failed');
  }
});

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

const start = async function(a, b) {
  try {
    var response = await axios.post(env.MATTERMOST_HOST+'/api/v4/users/login', {
      login_id: env.MATTERMOST_ADMIN_USER,
      password: env.MATTERMOST_ADMIN_PASSWORD,
    });

    var mmToken = response.headers.token;

    response = await axios.get(env.MATTERMOST_HOST+'/api/v4/oauth/apps', {
      headers: {
        "Authorization" : "Bearer "+mmToken
      }
    });

    var oauthApp;

    response.data.forEach(element => {
      if(element.name == env.MATTERMOST_APP_NAME) {
        oauthApp = element;
      }
    });

    if(oauthApp === undefined) {
      console.error("App not found")
      throw "App not found"
    }

    response = await axios.post(env.MATTERMOST_HOST+'/api/v4/oauth/apps/'+oauthApp.id+'/regen_secret', {}, {
      headers: {
        "Authorization" : "Bearer "+mmToken
      }
    });

    credentials = {
      client: {
        id: oauthApp.id,
        secret: response.data.client_secret
      },
      auth: {
        tokenHost: env.TOKEN_HOST,
        tokenPath: env.TOKEN_PATH,
        authorizePath: env.AUTHORIZE_PATH,
      },
      options: {
        authorizationMethod: 'body'
      }
    };

    // Initialize the OAuth2 Library
    oauth2 = require('simple-oauth2').create(credentials);

    return app;
  }
  catch (e) {
    console.error(e)
    process.exit(1)
  }
}

start().catch;

module.exports = app;
