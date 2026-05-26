# Cambios Realizados en Auth y Datos de Prueba

## 1. Problema inicial en autenticación

El flujo de autenticación tenía varios problemas:

- `UsuarioService.findByNombre(...)` devolvía un `Usuario` directo o `null`, lo que hacía más difícil manejar correctamente el caso de usuario inexistente.
- `UserDetailsService` no implementaba oficialmente la interfaz de Spring Security `org.springframework.security.core.userdetails.UserDetailsService`.
- La búsqueda usada por Spring Security para autenticar construía un `Usuario` parcial, sin `passwordHash`.
- La entidad `Usuario` no exponía correctamente la información requerida por `UserDetails`.
- El `AuthController` devolvía `500` incluso cuando el error real era credenciales inválidas.

## 2. Cambios en repositorio de usuarios

Archivo:
[UsuarioRepository.java](/E:/UPB/CERTI3/B2B-Proyecto/data/src/main/java/com/example/B2BProyect/repository/UsuarioRepository.java)

Cambios:

- Se eliminó la query parcial usada para autenticación:
  - `findByUserIdToValidateSession(...)`
- Se eliminó el método derivado anterior:
  - `findUsuarioByNombre(...)`
- Se dejó una sola búsqueda consistente:
  - `Optional<Usuario> findByNombre(String nombre);`

Motivo:

- Spring Security necesita el usuario completo, especialmente `passwordHash`, `activo` y `rol`.

## 3. Cambios en servicio de usuario

Archivo:
[UsuarioService.java](/E:/UPB/CERTI3/B2B-Proyecto/core/src/main/java/com/example/B2BProyect/service/UsuarioService.java)

Cambios:

- `findByNombre(...)` ahora devuelve `Optional<Usuario>`.
- Se marcó como `@Transactional(readOnly = true)`.

Motivo:

- Permite manejar correctamente usuario existente o inexistente sin depender de `null`.

## 4. Cambios en UserDetailsService

Archivo:
[UserDetailsService.java](/E:/UPB/CERTI3/B2B-Proyecto/core/src/main/java/com/example/B2BProyect/service/UserDetailsService.java)

Cambios:

- La clase ahora implementa:
  - `org.springframework.security.core.userdetails.UserDetailsService`
- `loadUserByUsername(...)` ahora consulta:
  - `usuarioRepository.findByNombre(nombre)`
- Si no encuentra usuario, lanza:
  - `UsernameNotFoundException`
- Devuelve directamente la entidad `Usuario`, ya que implementa `UserDetails`.

Motivo:

- Esto integra correctamente el servicio con `AuthenticationManager` y `DaoAuthenticationProvider`.

## 5. Cambios en la entidad Usuario

Archivo:
[Usuario.java](/E:/UPB/CERTI3/B2B-Proyecto/data/src/main/java/com/example/B2BProyect/repository/entity/Usuario.java)

Cambios:

- `getPassword()` ahora devuelve `passwordHash`.
- `getUsername()` ahora devuelve `nombre`.
- `isEnabled()` ahora devuelve `Boolean.TRUE.equals(activo)`.
- `getAuthorities()` ahora devuelve una autoridad real basada en el rol:
  - `ROLE_<NOMBRE_DEL_ROL>`

Antes:

- `getPassword()` devolvía vacío.
- `getUsername()` devolvía vacío.
- `getAuthorities()` construía una lista pero terminaba devolviendo una vacía.
- `isEnabled()` dependía del comportamiento por defecto, no del campo `activo`.

Motivo:

- Spring Security necesita estos métodos correctos para autenticar y autorizar.

## 6. Cambios en AuthController

Archivo:
[AuthController.java](/E:/UPB/CERTI3/B2B-Proyecto/B2B-api/src/main/java/com/example/B2BProyect/controller/AuthController.java)

Cambios:

- Se reescribió el flujo del método `auth(...)`.
- Ahora se busca el usuario con:
  - `userService.findByNombre(username).orElseThrow(...)`
- Si las credenciales son incorrectas, se responde con:
  - `401 Unauthorized`
- Si ocurre un error interno real, se responde con:
  - `500 Internal Server Error`

Motivo:

- Antes un error de login terminaba devolviendo `500`, lo cual no representaba correctamente el problema.

## 7. Problema posterior detectado

Después de corregir el flujo de autenticación, apareció este error:

`Given that there is no default password encoder configured, each password must have a password encoding prefix`

Eso indicó que los `password_hash` almacenados en base:

- no tenían prefijo como `{bcrypt}`
- y además los datos de prueba sembrados tenían hashes de ejemplo no válidos

## 8. Cambios en PasswordEncoder

Archivo:
[InjectConfiguration.java](/E:/UPB/CERTI3/B2B-Proyecto/B2B-api/src/main/java/com/example/B2BProyect/config/InjectConfiguration.java)

Cambios:

- Se mantuvo `DelegatingPasswordEncoder`.
- Se configuró un encoder por defecto para compatibilidad con hashes bcrypt sin prefijo:

```java
DelegatingPasswordEncoder passwordEncoder =
        (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
return passwordEncoder;
```

Motivo:

- Permite que Spring Security acepte tanto hashes con prefijo `{bcrypt}` como hashes bcrypt heredados sin prefijo.

## 9. Cambios en datos de prueba

Archivo:
[seed-test-data.sql](/E:/UPB/CERTI3/B2B-Proyecto/seed-test-data.sql)

Cambios:

- Se reemplazaron hashes falsos por un hash bcrypt real y válido.
- Se dejó el hash con prefijo `{bcrypt}`.

Contraseña configurada para usuarios de prueba:

- `Abc123**`

Usuarios actualizados:

- `Admin TechNova`
- `Comprador TechNova`

Hash configurado:

- `{bcrypt}$2a$10$GiTk0ms5obxTpxdOGTeufeKET6eOCeZgCbpV8UnBWs2ICufqnUN.G`

## 10. Cambios aplicados en la base de datos

Base usada:

- `jdbc:postgresql://certificacion-server/marketplace`

Acciones realizadas:

- Se creó el archivo `seed-test-data.sql`.
- Se cargaron datos de prueba directamente en la base.
- Se corrigieron errores del script según restricciones reales del esquema.
- Se reejecutó el seed para dejar usuarios, productos, órdenes y factura consistentes.
- Se verificó que los usuarios de prueba quedaron con `password_hash` válido.

## 11. Usuarios de prueba para login

Puedes probar autenticación con cualquiera de estos:

- Usuario: `Admin TechNova`
  - Contraseña: `Abc123**`

- Usuario: `Comprador TechNova`
  - Contraseña: `Abc123**`

## 12. Resumen final

Con estos cambios quedó corregido:

- el lookup del usuario para auth
- la integración con Spring Security
- la carga de `UserDetails`
- el manejo correcto de roles y estado activo
- la respuesta HTTP adecuada para credenciales inválidas
- el formato real y válido de los `password_hash` en base

## 13. Archivos modificados

- [AuthController.java](/E:/UPB/CERTI3/B2B-Proyecto/B2B-api/src/main/java/com/example/B2BProyect/controller/AuthController.java)
- [InjectConfiguration.java](/E:/UPB/CERTI3/B2B-Proyecto/B2B-api/src/main/java/com/example/B2BProyect/config/InjectConfiguration.java)
- [UserDetailsService.java](/E:/UPB/CERTI3/B2B-Proyecto/core/src/main/java/com/example/B2BProyect/service/UserDetailsService.java)
- [UsuarioService.java](/E:/UPB/CERTI3/B2B-Proyecto/core/src/main/java/com/example/B2BProyect/service/UsuarioService.java)
- [UsuarioRepository.java](/E:/UPB/CERTI3/B2B-Proyecto/data/src/main/java/com/example/B2BProyect/repository/UsuarioRepository.java)
- [Usuario.java](/E:/UPB/CERTI3/B2B-Proyecto/data/src/main/java/com/example/B2BProyect/repository/entity/Usuario.java)
- [seed-test-data.sql](/E:/UPB/CERTI3/B2B-Proyecto/seed-test-data.sql)
