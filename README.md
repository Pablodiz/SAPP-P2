# Práctica 2

## A) Implementación endpoint AA contra la BD local

El endpoint de autenticación se expone en UserController bajo la ruta POST /api/login, su responsabilidad es autenticar las credenciales recibidas y emitir un JWT que el cliente utiliza en las peticiones venideras.

Su implementación final respecto a la inicial dada fue la siguiente:

```
@RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<TokenResponse> doLogin(@RequestBody Credentials credentials) 
            throws AuthenticationException {

        final Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                credentials.getUsername(),
                credentials.getPassword()
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String token = tokenProvider.generateHs256SignedToken(userService.loadUserByUsername(credentials.getUsername()));

        return ResponseEntity.ok(new TokenResponse(token));
    }    
```

El flujo resultado se puede ver en cuatro pasos clave:
### 1. Recepción de credenciales y autenticación con `AuthenticationManager`

Este método recibe un objeto `Credentials` en el cuerpo de la petición (`@RequestBody Credentials credentials`), que encapsula el user/password introducidos en la aplicación.

Con esos datos se crea un `UsernamePasswordAuthenticationToken` que se entrega al `AuthenticationManager`, el cual verifica las credenciales contra la BD.

Si la autenticación es correcta, se devuelve un objeto `Authentication` marcado como autenticado, que contiene el *principal* (un UserDetails interno) y los roles asociados al usuario (`GrantedAuthority`).

En caso de credenciales incorrectas, se lanza una `AuthenticationException` que provoca una respuesta con error en la webapp.
### 2. Propagar la autenticación al `SecurityContext`

Cuando el `AuthenticationManager` valida las credenciales, el objeto `Authentication` resultante se almacena en el `SecurityContext`.

Así, el usuario autenticado pasa a estar disponible para el resto de la aplicación durante el ciclo de vida de la petición, de forma que cualquier componente pueda recuperar quién es el usuario y qué roles tiene a través de una consulta al `SecurityContext`.
### 3. Generación del JWT

Se crea el objeto `UserDetails`, en el que se carga la información del usuario, y que contiene el username y los roles del mismo. 

Este objeto es el que se entrega a `JwtTokenProvider` , que se encarga de crear el JWT con los claims correspondientes y luego firmarlo con la clave privada del servidor.
### 4. Respuesta HTTP

Finalmente, el token generado se devuelve al cliente encapsulado en un objeto `TokenResponse`, resultando en un 200 OK, para que el cliente reciba el JWT emitido y pueda almacenarlo y reutilizarlo en peticiones posteriores añadiéndolo en header `Authorization`.
