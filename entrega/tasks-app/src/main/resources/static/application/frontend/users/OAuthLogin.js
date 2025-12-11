var OAuthLogin = (props) => {

    if(location.search) {

        const urlParams = new URLSearchParams(location.search.substring(1));
        const code = urlParams.get("code");
        // FIXME.
        // Get codeVerifier from sessionStorage.
        const codeVerifier = sessionStorage.getItem('code_verifier');

        // FIXME.
        // Remove codeVerifier from sessionStorage.
        sessionStorage.removeItem('code_verifier');

        if (code && codeVerifier) {

            const tokenParams = new Map();

            tokenParams.set('grant_type', 'authorization_code');
            // FIXME.
            // Add rest of parameters to make the request to the token endpoint.
            tokenParams.set('code', code);
            tokenParams.set('code_verifier', codeVerifier);
            tokenParams.set('redirect_uri', 'http://127.0.0.1:8888/tasks-service/dashboard/loginOAuth');
            tokenParams.set('client_id', 'tasks_app');

            oauthService.getToken(tokenParams,
                response => {
                    const accessToken = response.access_token;
                    const jwtToken = jwt.parseJwtToken(accessToken);
                    jwt.storeJwtToken(accessToken);
                    // id_token will be required later to logout the user from
                    // the OpenID Provider (see Navbar.js).
                    sessionStorage.setItem("idToken", response.id_token);
                    props.dispatch({
                        type: 'login',
                        user: jwtToken.sub,
                        roles: jwtToken.roles,
                        token: accessToken
                    });
                },
                () => {
                    alerts.error('Access denied!');
                });

        }

        return (<ReactRouterDOM.Redirect to="/"/>);

    }

    alerts.error('Access denied!');
    return (<ReactRouterDOM.Redirect to="/"/>);

};

OAuthLogin = ReactRedux.connect()(OAuthLogin);