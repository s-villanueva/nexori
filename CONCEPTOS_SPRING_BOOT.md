# Conceptos de Spring Boot y Spring Security presentes en el proyecto

Este documento reúne los conceptos más importantes que ya aparecen en tu código. La idea no es solo listarlos, sino dejar claro qué son, por qué existen y cómo se aplican en este proyecto.

## 1. Capa web y controladores

### `@RestController`
Marca una clase como controlador REST. Spring la detecta como componente web y hace que los valores retornados por sus métodos se serialicen directamente a JSON o a otro formato de respuesta HTTP.

En la práctica, `@RestController` equivale a usar:

```java
@Controller
@ResponseBody
```

Se usa cuando tu backend expone una API y no vistas HTML. En tu proyecto, el caso más claro es [AuthController.java](/E:/UPB/CERTI3/B2B-Proyecto/B2B-api/src/main/java/com/example/B2BProyect/controller/AuthController.java), donde se devuelve un token y no una página.

### `@Controller`
También marca una clase como controlador web, pero por sí solo no implica que la respuesta sea JSON. Históricamente se usa más en aplicaciones MVC con vistas renderizadas en servidor.

En tu proyecto varios controladores CRUD fueron creados con `@Controller`. Funciona, pero como devuelven `ResponseEntity` y datos JSON, en muchos casos sería más natural usar `@RestController`.

### `@RequestMapping`
Define una ruta base para una clase o una ruta específica para un método. Sirve para decirle a Spring qué URL corresponde a qué controlador.

Ejemplo conceptual:

```java
@RequestMapping("/api/v1/usuarios")
```

Eso significa que los métodos internos cuelgan de esa base.

### `@GetMapping`
Mapea peticiones HTTP `GET`. Se usa normalmente para consultas o lecturas.

En tu proyecto aparece en métodos como `findAll()`, donde el cliente pide listar entidades.

### `@PostMapping`
Mapea peticiones HTTP `POST`. Se usa normalmente para crear recursos o iniciar procesos, como login.

Ejemplo:

```java
@PostMapping("/api/v1/auth")
```

### `@PatchMapping`
Mapea peticiones HTTP `PATCH`. Sirve para actualizaciones parciales, donde no reemplazas todo el recurso, solo algunos campos.

### `@RequestBody`
Le dice a Spring que lea el cuerpo JSON de la petición y lo convierta en un objeto Java.

Ejemplo:

```java
public ResponseEntity<?> token(@RequestBody AuthenticationDTO data)
```

Spring toma el JSON enviado, lo deserializa y te entrega un `AuthenticationDTO`.

### `@PathVariable`
Extrae un valor dinámico de la URL.

Ejemplo:

```java
@PatchMapping("/{id}")
```

Si llega `/usuarios/123`, Spring puede tomar ese `123` y convertirlo al tipo del parámetro.

### `ResponseEntity`
Es una forma explícita de construir respuestas HTTP. Te permite controlar:

- status code
- body
- headers

Ejemplos típicos:

```java
ResponseEntity.ok(data)
ResponseEntity.status(HttpStatus.CREATED).build()
ResponseEntity.badRequest().build()
```

### Manejo de códigos HTTP
Los códigos HTTP comunican el resultado real de la operación:

- `200 OK`: operación exitosa
- `201 Created`: recurso creado
- `400 Bad Request`: request inválido
- `401 Unauthorized`: credenciales inválidas o token no válido
- `403 Forbidden`: autenticado, pero sin permisos
- `500 Internal Server Error`: fallo interno real

Una idea importante es que el backend no solo debe funcionar: también debe responder con el código correcto.

### `ResponseStatusException`
Permite lanzar una excepción desde el controlador o el service y devolver un código HTTP específico.

Ejemplo:

```java
throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
```

Es útil cuando quieres cortar el flujo y responder inmediatamente con semántica HTTP clara.

## 2. Inyección de dependencias y configuración

### `@Service`
Marca una clase como capa de servicio. Conceptualmente, ahí vive la lógica de negocio o la coordinación entre repositorios.

En tu proyecto, `UsuarioService`, `EmpresaService` y otros encapsulan operaciones de persistencia y flujos más cercanos al dominio que a la capa HTTP.

### `@Component`
Marca una clase como bean administrado por Spring, sin especializarla como controller, service o repository.

Se usa para piezas de infraestructura o utilidades. Ejemplo en tu proyecto: el `DataInitializer`.

### `@Configuration`
Marca una clase como fuente de configuración de Spring. Normalmente contiene métodos `@Bean` para registrar objetos personalizados en el contenedor.

### `@Bean`
Le dice a Spring: “el objeto que retorna este método debe registrarse como bean”.

Lo usas, por ejemplo, para `PasswordEncoder` y `AuditorAware`.

### Inyección por constructor
Es la práctica de declarar dependencias como atributos `final` y recibirlas por constructor.

Ventajas:

- más fácil de testear
- obliga a que la dependencia exista
- hace visibles las dependencias reales de la clase

### Beans de Spring
Un bean es un objeto administrado por el contenedor de Spring. Spring se encarga de:

- crearlo
- configurarlo
- inyectarlo donde haga falta
- controlar su ciclo de vida

Cuando pones `@Service`, `@Component`, `@Controller` o registras algo con `@Bean`, estás creando beans.

### Ciclo de vida de beans
Spring crea los beans al arrancar la aplicación, resuelve dependencias y luego puede ejecutar lógica adicional de inicialización.

### `@PostConstruct`
Marca un método que debe ejecutarse después de que Spring haya construido e inyectado el bean.

En tu proyecto se usa en `JwtTokenProvider` para decodificar la llave secreta una sola vez al inicio.

### Configuración externa con `application.properties`
Permite definir parámetros fuera del código:

- puerto del servidor
- URL de base de datos
- usuario y contraseña
- propiedades JWT

Ventaja: cambias configuración sin recompilar la lógica.

### `@Value`
Inyecta una propiedad de configuración dentro de un bean.

Ejemplo:

```java
@Value("${security.jwt.token.secret-key}")
```

## 3. Lombok

Lombok reduce código repetitivo generado de forma automática en compilación.

### `@AllArgsConstructor`
Genera un constructor con todos los atributos.

### `@RequiredArgsConstructor`
Genera un constructor solo con campos `final` o `@NonNull`. Es muy útil para inyección por constructor.

### `@NoArgsConstructor`
Genera un constructor vacío. En JPA suele ser necesario para que Hibernate pueda instanciar entidades.

### `@Getter` y `@Setter`
Generan getters y setters automáticamente.

### `@Builder`
Permite construir objetos con una sintaxis más legible:

```java
Usuario.builder().nombre("root").email("x").build();
```

### `@Slf4j`
Genera un logger llamado `log`, para poder escribir:

```java
log.info("mensaje");
log.error("error", e);
```

## 4. Persistencia con Spring Data JPA

### Spring Data JPA
Es una capa que simplifica el acceso a la base de datos usando JPA/Hibernate. Te permite trabajar con repositorios en vez de escribir SQL manual para todo.

### `JpaRepository`
Es una interfaz base que ya trae operaciones comunes:

- `save`
- `findById`
- `findAll`
- `deleteById`

Cuando un repositorio extiende `JpaRepository`, hereda ese CRUD básico.

### Métodos derivados por nombre
Spring puede generar queries automáticamente según el nombre del método.

Ejemplo:

```java
Optional<Usuario> findByNombre(String nombre);
```

Spring interpreta “find by nombre” y arma la query correspondiente.

### Queries personalizadas con `@Query`
Se usan cuando la consulta no es trivial o quieres devolver DTOs/proyecciones.

Ejemplo típico en tu proyecto:

- construir un `UsuarioDTO`
- traer campos concretos
- evitar exponer la entidad completa

### `Optional`
Representa la posibilidad de que un valor exista o no.

Se usa mucho en repositorios con consultas que pueden no encontrar nada.

Ventaja:

- obliga a pensar el caso “no encontrado”
- evita parte del abuso de `null`

### DTOs de respuesta
Un DTO es un objeto pensado para transportar datos entre capas o hacia el cliente.

En tu proyecto aparecen muchos `...DTO`. Sirven para:

- no exponer entidades enteras
- elegir exactamente qué campos devolver
- desacoplar la API del modelo interno

### Proyecciones JPA
Permiten devolver subconjuntos de datos sin cargar toda la entidad.

Hay dos estilos visibles en tu proyecto:

- interfaces o projections
- DTOs construidos con `new ...(...)` en JPQL

### `@EntityGraph`
Permite decirle a JPA que, para una consulta concreta, cargue relaciones adicionales que normalmente son `LAZY`.

Lo usamos en el auth para traer `idRol` cuando se carga `Usuario`, evitando `LazyInitializationException` sin volver globalmente eager la relación.

## 5. Entidades JPA

### `@Entity`
Marca una clase como entidad persistente, o sea, un objeto que se mapea a una tabla.

### `@Table`
Permite especificar el nombre de la tabla y metadatos como índices o restricciones.

### `@Id`
Marca la clave primaria.

### `@Column`
Controla detalles del mapeo de una columna:

- nombre
- nulabilidad
- longitud
- precisión

### `@GeneratedValue`
Define cómo se genera el ID.

### `@Index`
Define índices a nivel de tabla. Mejoran performance de búsqueda, aunque no siempre se gestionan igual según el esquema real o Hibernate.

### `@UniqueConstraint`
Declara unicidad en columnas.

### `@ColumnDefault`
Expresa un valor por defecto a nivel de mapeo/DDL.

## 6. Relaciones entre entidades

### `@ManyToOne`
Muchos registros apuntan a uno.

Ejemplo:

- muchos `Usuario` pueden apuntar a un `RolUsuario`
- muchos `Producto` pueden apuntar a una `Categoria`

### `@OneToMany`
Uno a muchos. Es normalmente el lado inverso de un `ManyToOne`.

### `@OneToOne`
Un registro se relaciona con exactamente uno del otro lado.

### `@EmbeddedId`
Se usa cuando la clave primaria es compuesta.

### `@Embeddable`
Marca la clase que representa esa clave compuesta.

### `@MapsId`
Se usa cuando una entidad con clave compuesta usa relaciones que además forman parte de esa clave.

### `@JoinColumn`
Indica la columna FK usada para la relación.

## 7. Estrategias de carga

### `FetchType.LAZY`
La relación no se carga inmediatamente. Hibernate crea un proxy y solo trae la información si la usas.

Ventaja:

- mejor performance por defecto
- menos datos innecesarios

Riesgo:

- si accedes fuera de sesión/transacción, puede fallar con `LazyInitializationException`

### `FetchType.EAGER`
La relación se carga automáticamente junto con la entidad principal.

Ventaja:

- más simple cuando siempre necesitas esa relación

Riesgo:

- puede cargar demasiado en todo el sistema
- puede empeorar performance global

### Carga por defecto vs carga puntual
Aquí está la distinción importante:

- `LAZY` o `EAGER` definen la política general de la relación
- `EntityGraph` o `join fetch` cambian el comportamiento para una consulta concreta

### `LazyInitializationException`
Pasa cuando intentas acceder a una relación lazy y la sesión de Hibernate ya cerró.

Eso fue exactamente lo que te pasó con `Usuario -> RolUsuario` al validar JWT.

### Uso de `@EntityGraph`
Sirve para decir: “esta relación sigue siendo lazy normalmente, pero en esta consulta en particular la necesito ya cargada”.

## 8. Transacciones

### `@Transactional`
Marca que un método debe ejecutarse dentro de una transacción.

En una transacción JPA/Hibernate puede:

- persistir cambios
- hacer rollback si hay error
- mantener contexto de persistencia

### `readOnly = true`
Se usa cuando solo lees. Le comunica intención al framework y puede ayudar a optimizar.

### Persistencia dentro y fuera de transacción
Si accedes a entidades y relaciones dentro de la transacción, Hibernate puede resolver proxies lazy. Fuera de ella, pueden aparecer errores.

## 9. Separación por capas

### `controller`
Recibe requests HTTP y construye responses.

### `service`
Contiene lógica de negocio, coordinación y reglas.

### `repository`
Accede a la base de datos.

### `entity`
Representa el modelo persistente.

### `dto`
Representa datos de entrada o salida para API.

### Diferencia entre entidad y DTO
Una entidad existe para persistencia; un DTO existe para transporte de datos.

No siempre conviene exponer una entidad directamente al cliente, porque:

- puede traer relaciones innecesarias
- puede exponer campos internos
- acopla demasiado la API al modelo de base

## 10. Spring Security

### `SecurityFilterChain`
Define cómo se aplica la seguridad a las requests.

Allí configuras:

- qué rutas son públicas
- qué rutas requieren autenticación
- filtros
- CORS
- sesiones

### `AuthenticationManager`
Es el componente que intenta autenticar credenciales.

Cuando haces:

```java
authenticationManager.authenticate(...)
```

Spring valida usuario, password y estado.

### `UsernamePasswordAuthenticationToken`
Representa credenciales o autenticación ya resuelta.

Se usa tanto para:

- enviar `username/password` al `AuthenticationManager`
- guardar al usuario autenticado en el contexto

### `UserDetails`
Es la interfaz que Spring Security usa para representar a un usuario autenticable.

Tu entidad `Usuario` implementa esta interfaz.

### `UserDetailsService`
Es la interfaz que Spring usa para cargar un usuario por username.

Tu implementación actual es la pieza que conecta Spring Security con `UsuarioRepository`.

### `PasswordEncoder`
Se encarga de comparar contraseñas en texto plano contra hashes almacenados.

### `DelegatingPasswordEncoder`
Permite manejar varios formatos de hash mediante prefijos como:

- `{bcrypt}`
- `{noop}`

### `BCryptPasswordEncoder`
Implementa bcrypt. Es el algoritmo que terminaste usando para los usuarios de prueba.

### `GrantedAuthority`
Representa una autoridad reconocida por Spring Security.

### `SimpleGrantedAuthority`
Implementación concreta y simple de una authority, por ejemplo:

```java
ROLE_ADMIN
```

### `SecurityContextHolder`
Guarda el usuario autenticado en el contexto del hilo actual de la request.

## 11. JWT

### Generación de JWT
Consiste en crear un token firmado con información mínima del usuario.

En tu proyecto, el token se genera en `JwtTokenProvider`.

### Validación de JWT
Implica:

- verificar firma
- verificar expiración
- leer claims

### Claims
Son los datos dentro del token:

- subject
- id
- issued at
- expiration

### Expiración del token
Hace que el token deje de ser válido después de cierto tiempo.

### `OncePerRequestFilter`
Filtro que corre una vez por request. Es ideal para validar JWT.

### Filtro JWT personalizado
Tu `JwtTokenFilter` toma el header `Authorization`, resuelve el token y, si es válido, coloca la autenticación en el contexto.

## 12. Manejo de seguridad en requests

### Autenticación
Responde a: “¿quién eres?”

### Autorización
Responde a: “¿qué puedes hacer?”

### Roles
Son categorías de permisos, como `ADMIN`, `ROOT`, `COMPRADOR`.

### Authorities
Son la forma concreta en que Spring Security representa permisos o roles.

### Usuario autenticado en contexto
Una vez autenticado, el backend puede acceder al principal actual desde `SecurityContextHolder`.

## 13. CORS y filtros

### CORS
Controla qué orígenes pueden hacer requests al backend desde navegador.

### Filtros HTTP
Son piezas que interceptan requests y responses antes o después del controlador.

### `OncePerRequestFilter`
Es útil cuando no quieres que el filtro se ejecute múltiples veces en la misma request.

### Orden de filtros
El orden importa. Por eso en seguridad se define si un filtro va antes o después de otro, por ejemplo antes de `UsernamePasswordAuthenticationFilter`.

## 14. Auditoría y contexto

### `AuditorAware`
Permite decir quién es el usuario actual para operaciones de auditoría.

### `Authentication`
Representa la autenticación actual en Spring Security.

### Contexto del usuario actual
Se obtiene desde `SecurityContextHolder` y sirve para saber quién ejecuta una acción.

## 15. Inicialización y datos de prueba

### `DataInitializer`
Componente que puede crear datos iniciales al arrancar la aplicación.

### Carga de datos de prueba
Sirve para que el sistema tenga un estado funcional de arranque para pruebas o demos.

### Seed SQL
Un script SQL que inserta datos iniciales controlados y repetibles.

### Contraseñas hasheadas
Nunca se guarda la contraseña en texto plano. Se guarda su hash, y el login compara el texto recibido contra ese hash.

## 16. Manejo de errores y excepciones

### `BadCredentialsException`
Se usa cuando las credenciales son inválidas.

### `UsernameNotFoundException`
Se usa cuando el usuario no existe en el lookup de seguridad.

### `ResponseStatusException`
Permite devolver un código HTTP específico como parte del error.

### `InvalidJwtAuthenticationException`
Es tu excepción personalizada para JWT inválido.

### `NotDataFoundException`
Es otra excepción de dominio o infraestructura en tu proyecto.

### Excepciones de Hibernate
Aquí entran cosas como:

- `LazyInitializationException`
- errores de mapeo
- violaciones de restricciones

## 17. Conceptos prácticos que ya aparecieron en este proyecto

### Diferencia entre `null` y `Optional`
`null` no comunica intención y obliga a revisar manualmente. `Optional` expresa explícitamente “puede no existir”.

### Cuándo usar `LAZY`
Cuando una relación no siempre hace falta y quieres mantener el costo bajo por defecto.

### Cuándo usar `EAGER`
Cuando la relación casi siempre es necesaria junto con la entidad y eso es parte natural de su uso global.

### Cuándo usar `@EntityGraph`
Cuando la relación es `LAZY` por defecto, pero un flujo concreto necesita cargarla ya.

### Por qué un login debe responder `401` y no `500`
Porque credenciales inválidas no son un fallo interno del sistema; son un error de autenticación.

### Por qué un hash de contraseña necesita formato compatible con `PasswordEncoder`
Porque Spring debe saber con qué algoritmo fue generado para poder compararlo correctamente.

### Cómo una relación lazy puede romper auth o JWT
Si el usuario autenticado necesita datos del rol para construir authorities y el rol está lazy fuera de sesión, falla la validación.

## 18. Orden sugerido para estudiarlos

1. Capa web y controladores
2. Inyección de dependencias y configuración
3. Persistencia con JPA
4. Entidades y relaciones
5. `LAZY`, `EAGER` y `@EntityGraph`
6. Transacciones
7. Separación por capas
8. Spring Security
9. JWT
10. Manejo de errores

## 19. Recomendación práctica para este proyecto

Si tu objetivo es dominar este repo rápido, el mejor orden no es alfabético, sino por impacto:

1. entender controladores y `ResponseEntity`
2. entender services y repositories
3. entender entidades y relaciones
4. entender `LAZY`, `EAGER`, `EntityGraph` y transacciones
5. entender auth con Spring Security
6. entender JWT y filtros

Ese orden te permite pasar de “leer endpoints” a “entender por qué falló auth” de forma natural.
