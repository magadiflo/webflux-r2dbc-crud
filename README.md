# [WebFlux R2DBC Crud using PostgreSQL](https://www.youtube.com/watch?v=s6qKE0FD3BU&t=2137s)

Tutorial tomado del canal de youtube de **Joas Dev**.

---

## Dependencias

Las dependencias que se usaron en el proyecto son las siguientes:

````xml
<!--Spring Boot 3.2.2-->
<!--Java 21-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-r2dbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>r2dbc-postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.modelmapper</groupId>
        <artifactId>modelmapper</artifactId>
        <version>3.0.0</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Creando base de datos de Postgres en Docker usando compose

En la raíz del proyecto creamos el archivo `compose.yml` y agregamos la siguiente configuración para crear un
contenedor con la base de datos de PostgreSQL que usaremos en esta aplicación:

````yml
services:
  postgres:
    container_name: postgres
    image: postgres:15.2-alpine
    restart: unless-stopped
    environment:
      POSTGRES_DB: db_webflux_r2dbc
      POSTGRES_USER: magadiflo
      POSTGRES_PASSWORD: magadiflo
    ports:
      - 5433:5432
    expose:
      - 5432
````

Para levantar el contenedor, debemos posicionarnos mediante la terminal en la raíz donde se encuentra ubicado el archivo
`compose.yml` y luego ejecutar el comando siguiente:

````bash
M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\10.joas_dev\webflux-r2dbc-crud (feature/crud)
$ docker compose up -d
````

## [Configurando la base de datos R2DBC y PostgreSQL](https://www.bezkoder.com/spring-boot-r2dbc-postgresql/)

En el `application.yml` agregamos las siguientes configuraciones:

````yml
server:
  port: 8080

spring:
  application:
    name: webflux-r2dbc-crud

  r2dbc:
    url: r2dbc:postgresql://localhost:5433/db_webflux_r2dbc
    username: magadiflo
    password: magadiflo
````

Existe otra anotación que podríamos haberla incluído: `spring.data.r2dbc.repositories.enabled=true`, quien determina la
activación de los repositorios `R2DBC` en una aplicación Spring Boot. **De forma predeterminada, la compatibilidad con
el repositorio R2DBC está habilitada en una aplicación Spring Boot.** Si desea deshabilitar la compatibilidad con el
repositorio R2DBC, puede establecer la propiedad en falso de la siguiente manera:
`spring.data.r2dbc.repositories.enabled=false`.

La URL de conexión está configurada en `r2dbc:postgresql://localhost:5433/db_webflux_r2dbc`, donde:

- `r2dbc`, indicamos que usaremos `r2dbc` para conectarnos a la base de datos. Recordar que cuando usamos una aplicación
  normal de Spring Boot (no reactiva) usamos `jdbc`.
- `db_webflux_r2dbc`, es el nombre de la base de datos que creamos en Docker.
- `5433`, es el puerto que estamos exponiendo del contenedor de PostgreSQL. Recordar que el puerto por defecto de una
  base de datos de PostgreSQL es `5432`, pero en mi caso, como estoy usando contenedores de Docker, el puerto que expone
  el contenedor de mi base de datos lo configuré al `5433`.

Las propiedades `spring.r2dbc.username` y `spring.r2dbc.password` proporcionan las credenciales para conectarse a la
base de datos.

### @EnableR2dbcRepositories

`@EnableR2dbcRepositories` es una anotación Spring que se utiliza para habilitar repositorios R2DBC en una aplicación
Spring Boot. Proporciona una manera conveniente de crear una capa de repositorio en una aplicación Spring Boot que usa
R2DBC para interactuar con una base de datos.

> Debido a que la compatibilidad con el repositorio R2DBC está habilitada en nuestra aplicación Spring Boot de forma
> predeterminada (`spring.data.r2dbc.repositories.enabled=true`), **@EnableR2dbcRepositories no es necesario.**

La anotación `@EnableR2dbcRepositories` podría agregarse a una clase de configuración en su aplicación, generalmente a
la clase principal que está anotada con `@SpringBootApplication`.

## Habilitando WebFlux

Si usa `spring-boot-starter-webflux`, la configuración se realiza automáticamente a través de
`ReactiveWebServerFactoryAutoConfiguration` y `WebFluxAutoConfiguration`, por lo que no necesita anotar una
clase de configuración con `@EnableWebFlux`.

Cuando usa `spring-webflux` sin `spring-boot`, **necesita agregar `@EnableWebFlux` en una clase `@Configuration`
para importar la configuración de `Spring WebFlux` desde `WebFluxConfigurationSupport`.**

### ¿Qué hace la anotación @EnableWebFlux?

Usamos `@EnableWebFlux` para habilitar la compatibilidad con aplicaciones web reactivas utilizando el framework de
`Spring WebFlux`. Agregar esta anotación a una clase `@Configuration` importa la configuración de `Spring WebFlux`
desde `WebFluxConfigurationSupport` que permite el uso de controladores anotados y endpoints funcionales.

> Por lo tanto, en la mayoría de los casos, cuando estás construyendo una aplicación `Spring Boot con Spring WebFlux`,
> **no necesitas anotar tu clase de configuración principal con** `@EnableWebFlux`, ya que Spring Boot se encargará de
> habilitar WebFlux automáticamente.

## Habilitando logging para ver los queries y parameters en las consultas a PostgreSQL

````yml
logging:
  level:
    io.r2dbc.postgresql.QUERY: DEBUG
    io.r2dbc.postgresql.PARAM: DEBUG
````

## Creando e inicializando esquema de la base de datos

En el directorio `/resources` crearemos un archivo llamado `schema.sql` donde definiremos las tablas `authors` y
`books` y su relación de muchos a muchos, con el que generamos una tabla intermedia `book_authors`:

````sql
CREATE TABLE IF NOT EXISTS authors(
    id SERIAL,
    first_name VARCHAR(45) NOT NULL,
    last_name VARCHAR(45) NOT NULL,
    birthdate DATE NOT NULL,
    CONSTRAINT pk_authors PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS books(
    id SERIAL,
    title VARCHAR(255) NOT NULL,
    publication_date DATE NOT NULL,
    online_availability BOOLEAN DEFAULT FALSE,
    CONSTRAINT pk_books PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS book_authors(
    book_id INTEGER NOT NULL,
    author_id INTEGER NOT NULL,
    CONSTRAINT fk_books_book_authors FOREIGN KEY(book_id) REFERENCES books(id),
    CONSTRAINT fk_authors_book_authors FOREIGN KEY(author_id) REFERENCES authors(id)
);
````

### Inicializando esquema

Crearemos una clase de configuración `/configuration/SchemaConfig.java` donde definiremos un `@Bean` que nos retornará
un objeto del tipo `ConnectionFactoryInitializer`.

`Spring Data R2DBC ConnectionFactoryInitializer` proporciona una manera conveniente de configurar e inicializar una
fábrica de conexiones para una conexión de base de datos reactiva en una aplicación Spring. Escaneará el `schema.sql`
en el classpath y ejecutará el script SQL para inicializar la base de datos cuando la base de datos esté conectada.

````java

@Configuration
public class SchemaConfig {
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ClassPathResource resource = new ClassPathResource("schema.sql");
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator(resource);

        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(resourceDatabasePopulator);
        return initializer;
    }
}
````

> Si hasta este punto ejecutamos la aplicación, veremos que la ejecución es exitosa y las tablas se crean correctamente.

## Definiendo Modelo de Datos

Crearemos los modelos de datos correspondientes a las tablas que definimos en el `schema.sql`. Estas clases serán
creadas en el directorio `/persistence/entity`:

````java

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "authors")
public class Author {
    @Id
    private Integer id;
    private String firstName;
    private String lastName;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate birthdate;
}
````

````java

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "books")
public class Book {
    @Id
    private Integer id;
    private String title;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate publicationDate;
    private Boolean onlineAvailability;
}
````

````java

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "book_authors")
public class BookAuthor {
    private Integer bookId;
    private Integer authorId;
}
````

> Con respecto a la clase de entidad `BookAuthor`, el tutor del curso menciona que en `WebFlux` no ha
> encontrado la manera de que exista un mapeo de `muchos a muchos`, así que lo que optará por hacer es crear una clase
> de entidad donde definamos los dos atributos que tiene la tabla `book_authors` y posteriormente las consultas
> realizarlas con SQL nativo.
>
> Otro punto que menciona el autor es que la idea de una tabla detalle (resultante de la relación de muchos a muchos),
> es que no tenga una clave primaria, sino más bien solo tenga las claves foráneas correspondientes a las tablas
> principales.

## Creando interfaces de Repositorio

Crearemos un repositorio para cada entidad principal, en nuestro caso para `Author y Book`. Estos repositorios nos
permitirán interactuar con `authors y books` desde la base de datos de PostgreSQL.

Estos dos repositorios serán creados en el package `/persistence/repository` e implementarán la interfaz
`ReactiveCrudRepository` quien nos permitirá usar sus métodos ya definidos: `save(), findById(), findAll(), count(),
delete(), deleteById(), deleteAll(), etc.`

A continuación se muestra la creación del repositorio `IBookRepository` para la entidad `Book`:

````java
public interface IBookRepository extends ReactiveCrudRepository<Book, Integer> {
}
````

Ahora, mostramos la creación del repositorio `IAuthorRepository` para la entidad `Author`:

````java

public interface IAuthorRepository extends ReactiveCrudRepository<Author, Integer> {

    /**
     * @param author
     * @return affectedRows
     */
    @Modifying
    @Query(value = """
            INSERT INTO authors(first_name, last_name, birthdate)
            VALUES(:#{#author.firstName}, :#{#author.lastName}, :#{#author.birthdate})
            """)
    Mono<Integer> saveAuthor(@Param(value = "author") Author author);

    /**
     * @param author
     * @return affectedRows
     */
    @Modifying
    @Query(value = """
            UPDATE authors
            SET first_name = :#{#author.firstName},
                last_name = :#{#author.lastName},
                birthdate = :#{#author.birthdate}
            WHERE id = :#{#author.id}
            """)
    Mono<Integer> updateAuthor(@Param(value = "author") Author author);

    @Query("""
            SELECT a.id, a.first_name, a.last_name, CONCAT(a.first_name, ' ', a.last_name) AS full_name, a.birthdate
            FROM authors AS a
            WHERE a.id = :authorId
            """)
    Mono<IAuthorProjection> findByAuthorId(@Param(value = "authorId") Integer authorId);

    @Query(value = """
            SELECT a.id,
                    a.first_name,
                    a.last_name,
                    a.birthdate
            FROM authors AS a
            WHERE a.id IN(:authorIds)
            """)
    Flux<Author> findAllAuthorsByIdIn(List<Integer> authorIds);

    @Query(value = """
            SELECT COUNT(a.id)
            FROM authors AS a
            WHERE a.first_name LIKE :#{'%' + #query + '%'}
                OR a.last_name LIKE :#{'%' + #query + '%'}
            """)
    Mono<Integer> findCountByQuery(String query);

    @Query(value = """
            SELECT  a.id,
                    a.first_name,
                    a.last_name,
                    a.birthdate
            FROM authors AS a
            WHERE a.first_name LIKE :#{'%' + #query + '%'}
                OR a.last_name LIKE :#{'%' + #query + '%'}
            ORDER BY a.id ASC
            LIMIT :#{#pageable.getPageSize()}
            OFFSET :#{#pageable.getOffset()}
            """)
    Flux<IAuthorProjection> findByQuery(String query, Pageable pageable);
}
````

En la interfaz anterior hemos definido métodos personalizados, donde:

- Usamos la anotación `@Query()` para definir nuestra consulta.


- La consulta usada en la anotación `@Query()` es `SQL nativo`, ya que estamos trabajando con `Spring Data R2DBC`
  y no con `Spring Data JPA`, aunque dicho sea de paso, con `Spring Data JPA` también se puede trabajar con `SQL nativo`
  solo que en ese caso es necesario agregar el atributo `nativeQuery` en la anotación de la siguiente manera
  `@Query(value = "TU_CONSULTA_SQL", nativeQuery = true)`, mientras que con `Spring Data R2DBC` usamos
  la consulta SQL directamente en la anotación.


- La anotación `@Modifying` indica que un método de consulta debe considerarse una consulta de modificación que no
  devuelve nada o la cantidad de `filas afectadas` por la consulta. Los métodos de consulta anotados con `@Modifying`
  suelen ser instrucciones `INSERT, UPDATE, DELETE y DDL` que no devuelven resultados tabulares.


- Normalmente, cuando definimos parámetros a nuestros métodos de repositorio, si son pocos parámetros podemos definirlos
  uno a uno, pero si son muchos parámetros, podemos pasarle directamente un objeto que tendrá las propiedades que
  usaremos en la consulta. En nuestro caso, observemos la firma de nuestro método `saveAuthor()`
  `Mono<Integer> saveAuthor(@Param(value = "author") Author author)`, le estamos pasando la clase `Author`.


- Para usar las propiedades del objeto pasado por parámetro dentro de la consulta SQL usamos `SpEL`, por ejemplo:
  `:#{#author.firstName}`, donde `author` es el parámetro definido en el método y `firstName` es la propiedad del
  objeto. Esta sintaxis se utiliza para acceder a expresiones `SpEL (Spring Expression Language)`. Permite referenciar
  propiedades y métodos de objetos directamente en la consulta.

- El prefijo `#{}` indica que se está utilizando `SpEL`, y el símbolo `#` se utiliza para acceder a los parámetros del
  método o a las propiedades del objeto. Por ejemplo. `:#{#pageable.getPageSize()}` accede al método `getPageSize()` del
  objeto `Pageable` pasado como parámetro.

**NOTA**

En la siguiente consulta que hemos creado:

````java

@Query(value = """
        SELECT  a.id,
                a.first_name,
                a.last_name,
                a.birthdate
        FROM authors AS a
        WHERE a.first_name LIKE :#{'%' + #query + '%'}
            OR a.last_name LIKE :#{'%' + #query + '%'}
        ORDER BY a.id ASC
        LIMIT :#{#pageable.getPageSize()}
        OFFSET :#{#pageable.getOffset()}
        """)
Flux<IAuthorProjection> findByQuery(String query, Pageable pageable);
````

Estamos pasando por parámetro un `String query` y un `Pageable pageable`. Centrémonos en el objeto `pageable`. Estamos
agregando este objeto `pageable` por parámetro con la única finalidad de poder usar los valores internos que nos
proporcione su implementación. En otras palabras, lo que pasemos por parámetro del método `findByQuery()` es una
variable `query` y la implementación de la interfaz `Pageable`. Esta implementación la podemos obtener de un
`PageRequest.of(pageNumber, pageSize)`. Internamente, la implementación hace ciertas operaciones, las mismas que podemos
obtenerlas, por ejemplo con el `getOffset()` que es la multiplicación del `pageNumber * pageSize`.

Por otro lado, algo importante que se debe resaltar en las consultas personalizadas del repositorio anterior es que en
los métodos `findByAuthorId` y `findByQuery` estamos usando el concepto de `Projections` (a modo de ejemplo), con
`projections` podemos recuperar del total de columnas que tenga una tabla, solo las columnas que queramos. A
continuación, veamos el tema más detalladamente:

### [Projections](https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html)

Los métodos de consulta de Spring Data generalmente devuelven una o varias instancias de la raíz agregada administrada
por el repositorio. Sin embargo, a veces puede resultar conveniente crear proyecciones basadas en ciertos atributos de
esos tipos. Spring Data permite modelar tipos de retorno dedicados para recuperar de manera más selectiva vistas
parciales de los agregados administrados.

> Supongamos que tenemos una entidad con múchos atributos, unas 100 por ejemplo, por exagerar. Ahora, imagine que
> queremos recuperar únicamente 3 atributos ¿cómo lo haríamos?

### Proyecciones basadas en interfaz

La forma más sencilla de limitar el resultado de las consultas solo a los atributos seleccionados es declarando una
interfaz que exponga los métodos de acceso para que se lean las propiedades, como se muestra en el siguiente ejemplo:

Supongamos que tenemos el siguiente repositorio y su aggregate root:

````java
class Person {

    @Id
    UUID id;
    String firstname, lastname;
    Address address;

    static class Address {
        String zipCode, city, street;
    }
}

interface PersonRepository extends Repository<Person, UUID> {
    Collection<Person> findByLastname(String lastname);
}
````

Ahora, usando **proyecciones basadas en interfaz** definimos únicamente los atributos que queremos recuperar, por
ejemplo, recuperar únicamente los atributos del nombre de la persona:

````java
interface NamesOnly {
    String getFirstname();

    String getLastname();
}
````

Lo importante aquí es que las propiedades definidas aquí coinciden exactamente con las propiedades del aggregate root.
Al hacerlo, se puede agregar un método de consulta de la siguiente manera:

````java
// Un repositorio que utiliza una proyección basada en interfaz con un método de consulta
interface PersonRepository extends Repository<Person, UUID> {
    Collection<NamesOnly> findByLastname(String lastname);
}
````

#### Proyecciones cerradas

Una interfaz de proyección cuyos métodos de acceso coinciden con las propiedades del agregado de destino se considera
una proyección cerrada. El siguiente ejemplo (que también utilizamos anteriormente en este capítulo) es una proyección
cerrada.

````java
interface NamesOnly {
    String getFirstname();

    String getLastname();
}
````

#### Proyecciones abiertas

Los métodos de acceso en las interfaces de proyección también se pueden utilizar para calcular nuevos valores mediante
la anotación @Value, como se muestra en el siguiente ejemplo:

````java
interface NamesOnly {
    @Value("#{target.firstname + ' ' + target.lastname}")
    String getFullName();
    /*...*/
}
````

La raíz agregada que respalda la proyección está disponible en la variable objetivo. Una interfaz de proyección que
utiliza @Value es una proyección abierta. Spring Data no puede aplicar optimizaciones de ejecución de consultas en este
caso, porque la expresión SpEL podría usar cualquier atributo de la raíz agregada.

Las expresiones utilizadas en @Value no deben ser demasiado complejas; debe evitar la programación en variables de
cadena. Para expresiones muy simples, una opción podría ser recurrir a métodos predeterminados (introducidos en Java 8),
como se muestra en el siguiente ejemplo:

````java
// Una interfaz de proyección que utiliza un método predeterminado para lógica personalizada
interface NamesOnly {

    String getFirstname();

    String getLastname();

    default String getFullName() {
        return getFirstname().concat(" ").concat(getLastname());
    }
}
````

## Creando Interfaz de Projection

Luego de haber explicado en qué consisten las proyecciones, a continuación se muestran las que creamos y usamos en el
repositorio `IAuthorRepository`:

````java

@JsonPropertyOrder(value = {"id", "firstName", "lastName", "fullName", "birthdate"})
public interface IAuthorProjection {
    Integer getId();

    String getFirstName();

    String getLastName();

    default String getFullName() {
        if (getFirstName() == null || getLastName() == null) {
            return "";
        }
        return "%s %s".formatted(getFirstName(), getLastName());
    }

    @JsonFormat(pattern = "dd-MM-yyyy")
    LocalDate getBirthdate();
}
````

También se creó la proyección `IBookProjection` que será usado más adelante:

````java
public interface IBookProjection {
    Integer getId();

    String getTitle();

    @JsonFormat(pattern = "dd-MM-yyyy")
    LocalDate getPublicationDate();

    Boolean getOnlineAvailability();

    @JsonIgnore
    String getConcatAuthors();

    default List<String> getAuthors() {
        if (getConcatAuthors() == null || getConcatAuthors().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(getConcatAuthors().split(","));
    }
}
````

## Creando DTOs

A continuación mostramos todos los dtos creados usando `record` o `clases`. Los primeros records creados son para las
consultas que realizaremos usando `criteria`, es decir consultas que serán elaboradas de manera dinámica:

````java
public record BookCriteria(String q, LocalDate publicationDate) {
}
````

````java
public record AuthorCriteria(String firstName, Boolean lastName) {
}
````

````java
public record AuthorFilter(String q) {
}
````

Para el siguiente dto utilizaremos una clase con anotaciones de lombook. La razón del porqué usamos una clase y no un
record es porque esta clase será mapeada usando la dependencia de `ModelMapper` y según las pruebas que hice, la
dependencia no trabaja bien con records sino más bien con clases. Si usamos record, posiblemente el mapeo no va a
fallar, pero al inspeccionar el valor de los atributos mapeados, todos estarán en `null`.

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class RegisterBookDTO {
    private String title;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate publicationDate;
    private List<Integer> authors;
    private Boolean onlineAvailability = false;
}
````

Para el siguiente dto no vamos a usar un `record` sino más bien una clase normal de java. La razón del porqué este
record se creará usando una clase normal de java es porque vamos a usarlo para mapearlo a una entidad del tipo `Author`
y para dicho mapeo vamos a usar la dependencia de `ModelMapper`. Esta dependencia nos ayudará a mapear automáticamente
una clase java en otra.

Si uso un `record` con la dependencia `ModelMapper`, el mapeo va a fallar, los atributos de la clase de destino se
mostrarán en `null`, en otras palabras, según las pruebas que hice, `ModelMapper` no soporta el uso de `records`
para realizar el mapeo.

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class RegisterAuthorDTO {
    private String firstName;
    private String lastName;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate birthdate;
}
````

El siguiente record también será creado usando una clase simple de java por la misma razón que se mencionó
en el apartado superior.

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class UpdateAuthorDTO {
    private String firstName;
    private String lastName;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate birthdate;
}
````

## Creando clases adicionales

Crearemos una clase llamada `ApiException` con el que unificaremos los mensajes de error y el status del mismo. Es
importante que nuestra clase de excepción extienda de `RuntimeException`, ya que estamos trabajando con programación
reactiva:

````java

@Getter // Anotación de lombok
public class ApiException extends RuntimeException {

    private final String message;
    private final HttpStatus httpStatus;

    public ApiException(String message, HttpStatus httpStatus) {
        super(message);
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
````

Creamos la clase `AppConfig` donde definiremos el `@Bean` del `ModelMapper` que nos retornará una instancia de esa
clase:

````java

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
````

## Creando Servicios

Se definirán las interfaces y las implementaciones de de las entidades Book y Author. Solo implementaremos el servicio
`AuthorServiceImpl`:

````java
public interface IAuthorService {
    Flux<Author> findAll(AuthorCriteria authorCriteria);

    Mono<IAuthorProjection> findAuthorById(Integer authorId);

    Mono<Page<IAuthorProjection>> findAllToPage(String query, int pageNumber, int pageSize);

    Mono<Integer> saveAuthor(RegisterAuthorDTO registerAuthorDTO);

    Mono<IAuthorProjection> updateAuthor(Integer authorId, UpdateAuthorDTO updateAuthorDTO);

    Mono<Boolean> deleteAuthor(Integer authorId);
}
````

````java
public interface IBookService {
    Mono<Page<IBookProjection>> findAllToPage(BookCriteria bookCriteria, Pageable pageable);

    Mono<IBookProjection> findBookById(Integer bookId);

    Mono<Integer> saveBook(RegisterBookDTO registerBookDTO);

    Mono<Boolean> deleteBook(Integer bookId);
}
````

Ahora implementaremos las interfaces anteriores:

````java

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements IAuthorService {

    private final IAuthorRepository authorRepository;
    private final IBookAuthorDao bookAuthorDao;
    private final AuthorMapper authorMapper;

    @Override
    @Transactional(readOnly = true)
    public Flux<Author> findAll(AuthorCriteria authorCriteria) {
        return this.authorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<IAuthorProjection> findAuthorById(Integer authorId) {
        return this.authorRepository.findByAuthorId(authorId)
                .switchIfEmpty(Mono.error(new ApiException("No hay resultados con authorId: %d".formatted(authorId), HttpStatus.NOT_FOUND)));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Page<IAuthorProjection>> findAllToPage(String query, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Mono<Integer> totalRecords = this.authorRepository.findCountByQuery(query);

        return this.authorRepository.findByQuery(query, pageable)
                .collectList()
                .zipWith(totalRecords, (authorProjections, total) -> new PageImpl<>(authorProjections, pageable, total));
    }

    @Override
    @Transactional
    public Mono<Integer> saveAuthor(RegisterAuthorDTO registerAuthorDTO) {
        return Mono.just(registerAuthorDTO)
                .flatMap(dto -> this.authorMapper.toAuthor(registerAuthorDTO))
                .flatMap(this.authorRepository::saveAuthor)
                .doOnNext(affectedRows -> log.info("Filas afectadas en el insert: {}", affectedRows));
    }

    @Override
    @Transactional
    public Mono<IAuthorProjection> updateAuthor(Integer authorId, UpdateAuthorDTO updateAuthorDTO) {
        return this.authorRepository.findById(authorId)
                .flatMap(authorDB -> this.authorMapper.toAuthor(updateAuthorDTO, authorId))
                .flatMap(this.authorRepository::updateAuthor)
                .doOnNext(affectedRows -> log.info("Filas afectadas en el update: {}", affectedRows))
                .flatMap(affectedRows -> this.authorRepository.findByAuthorId(authorId))
                .switchIfEmpty(Mono.error(new ApiException("No se encontró el author con id %s para actualizar".formatted(authorId), HttpStatus.NOT_FOUND)));
    }

    @Override
    @Transactional
    public Mono<Boolean> deleteAuthor(Integer authorId) {
        return this.authorRepository.findById(authorId)
                .flatMap(authorDB -> this.bookAuthorDao.existBookAuthorByAuthorId(authorId))
                .flatMap(existsBookAuthor -> {
                    log.info("Existe el author en la tabla book_authors?: {}", existsBookAuthor);
                    if (existsBookAuthor) {
                        return this.bookAuthorDao.deleteBookAuthorByAuthorId(authorId).then(Mono.just(true));
                    }
                    return Mono.just(true);
                })
                .flatMap(canContinue -> this.authorRepository.deleteById(authorId).then(Mono.just(true)))
                .switchIfEmpty(Mono.error(new ApiException("No se encontró el author con id %s para eliminar".formatted(authorId), HttpStatus.NOT_FOUND)));
    }
}

````

La clase de servicio anterior está haciendo uso de una clase `AuthorMapper` para realizar el mapeo de un dto a una
entidad persistente. Esta clase, además es la que usa la dependencia que agregamos en el `pom.xml`, el `ModelMapper`
quien nos facilitará en hacer el mapeo de una clase dto a una clase de tipo persistente.

````java

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthorMapper {

    private final ModelMapper modelMapper;

    public Mono<Author> toAuthor(RegisterAuthorDTO registerAuthor) {
        try {
            Author author = this.modelMapper.map(registerAuthor, Author.class);
            return Mono.just(author);
        } catch (Exception e) {
            log.error("Error en mapeo para registrar author:: {}", e.getMessage());
            return Mono.error(new ApiException("Error al mapear entidad Author", HttpStatus.BAD_REQUEST));
        }
    }

    public Mono<Author> toAuthor(UpdateAuthorDTO registerAuthor, Integer authorId) {
        try {
            Author author = this.modelMapper.map(registerAuthor, Author.class);
            author.setId(authorId);
            return Mono.just(author);
        } catch (Exception e) {
            log.error("Error en mapeo para actualizar author:: {}", e.getMessage());
            return Mono.error(new ApiException("Error al mapear entidad Author", HttpStatus.BAD_REQUEST));
        }
    }
}
````

La clase de servicio `AuthorServiceImpl` también usa la implementación de la interfaz `IBookAuthorDao`, misma que
a continuación pasamos a documentar.

````java
public interface IBookAuthorDao {
    Mono<Long> findCountBookAuthorByCriteria(BookCriteria bookCriteria);

    Mono<Long> saveBookAuthor(BookAuthor bookAuthor);

    Mono<Void> saveAllBookAuthor(List<BookAuthor> bookAuthor);

    Mono<IBookProjection> findByBookId(Integer bookId);

    Mono<Boolean> existBookAuthorByBookId(Integer bookId);

    Mono<Boolean> existBookAuthorByAuthorId(Integer authorId);

    Mono<IBookProjection> findAllBookAuthorByBookId(Integer bookId);

    Mono<Void> deleteBookAuthorByBookId(Integer bookId);

    Mono<Void> deleteBookAuthorByAuthorId(Integer authorId);

    Flux<IBookProjection> findAllToPage(BookCriteria bookCriteria, Pageable pageable);
}
````

A continuación se muestra la implementación de la interfaz anterior:

````java

@Slf4j
@RequiredArgsConstructor
@Repository
public class BookAuthorDaoImpl implements IBookAuthorDao {

    /**
     * DatabaseClient, su símil sería jdbcTemplate, aquí usamos SQL nativo para hacer las consultas.
     */
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Long> findCountBookAuthorByCriteria(BookCriteria bookCriteria) {
        String select = "SELECT COUNT(b.id) AS result ";
        String from = "FROM books b ";
        String where = "";

        StringBuilder sqlWhere = new StringBuilder();
        boolean flag = false;

        if (StringUtils.hasText(bookCriteria.q())) {

            if (flag) {
                sqlWhere.append("OR ");
            }

            sqlWhere.append("b.title LIKE :q ");
            flag = true;
        }

        if (bookCriteria.publicationDate() != null) {

            if (flag) {
                sqlWhere.append("OR ");
            }

            sqlWhere.append("b.publicationDate = :publicationDate ");
            flag = true;
        }

        if (flag) {
            where = sqlWhere.insert(0, "WHERE ").toString();
        }

        String sql = select + from + where;
        log.info(sql);

        DatabaseClient.GenericExecuteSpec ges = databaseClient.sql(sql);

        if (StringUtils.hasText(bookCriteria.q())) {
            ges = ges.bind("q", "%" + bookCriteria.q() + "%");
        }

        if (bookCriteria.publicationDate() != null) {
            ges = ges.bind("publicationDate", bookCriteria.publicationDate());
        }

        return ges.map((row, metadata) -> {
                    log.info("count result {}", metadata.getColumnMetadata("result").toString());
                    return row.get("result", Long.class);
                }).first()
                .switchIfEmpty(Mono.error(new ApiException("No record found for book", HttpStatus.NOT_FOUND)));
    }

    @Override
    public Mono<Long> saveBookAuthor(BookAuthor bookAuthor) {
        return this.databaseClient.sql("""
                        INSERT INTO book_authors(book_id, author_id)
                        VALUES(:bookId, :authorId)
                        """)
                .bind("bookId", bookAuthor.getBookId())
                .bind("authorId", bookAuthor.getAuthorId())
                .fetch()
                .rowsUpdated()
                .onErrorMap(error -> new ApiException("Error al insertar en la tabla book_authors" + error.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @Override
    public Mono<Void> saveAllBookAuthor(List<BookAuthor> bookAuthorList) {
        List<Mono<Long>> inserts = bookAuthorList.stream()
                .map(bookAuthorToSave -> this.databaseClient.sql("""
                                INSERT INTO book_authors(book_id, author_id)
                                VALUES(:bookId, :authorId)
                                """)
                        .bind("bookId", bookAuthorToSave.getBookId())
                        .bind("authorId", bookAuthorToSave.getAuthorId())
                        .fetch()
                        .rowsUpdated()
                        .onErrorMap(error -> new ApiException(error.getMessage(), HttpStatus.BAD_REQUEST))
                )
                .toList();

        Flux<Long> concat = Flux.concat(inserts);
        return concat.then();
    }

    @Override
    public Mono<IBookProjection> findByBookId(Integer bookId) {
        String sql = """				
                SELECT ba.book_id as bookId, b.title as title, b.publication_date as publicationDate, b.online_availability as onlineAvailability,
                        STRING_AGG(a.first_name||' '||a.last_name, ', ') as concatAuthors
                FROM book_authors ba
                    INNER JOIN books b ON ba.book_id = b.id
                    INNER JOIN authors a ON ba.author_id = a.id
                WHERE b.id = :bookId
                GROUP BY ba.book_id, b.title, b.publication_date, b.online_availability
                """;

        return databaseClient.sql(sql)
                .bind("bookId", bookId)
                .map((row, metadata) -> {

                    log.info("publicationDate {} ", metadata.getColumnMetadata("publicationDate"));

                    return (IBookProjection) BookVO.builder()
                            .id(row.get("bookId", Integer.class))
                            .title(row.get("title", String.class))
                            //.publicationDate(row.get("publicationDate",LocalDateTime.class) != null ? row.get("publicationDate",LocalDateTime.class).toLocalDate() : null)
                            .publicationDate(row.get("publicationDate", LocalDate.class))
                            .onlineAvailability(row.get("onlineAvailability", Boolean.class))
                            .concatAuthors(row.get("concatAuthors", String.class))
                            .build();
                }).first()
                .switchIfEmpty(Mono.error(new ApiException("No se encontraron registros para el libro con id: " + bookId, HttpStatus.NOT_FOUND)));
    }

    @Override
    public Mono<Boolean> existBookAuthorByBookId(Integer bookId) {
        String sql = """				
                SELECT CASE
                          WHEN COUNT(ba.book_id) > 0 THEN true
                          ELSE false
                       END as result
                FROM book_authors AS ba
                WHERE ba.book_id = :bookId
                """;

        return databaseClient.sql(sql)
                .bind("bookId", bookId)
                .map((row, metadata) -> row.get("result", Boolean.class))
                .first();
    }

    @Override
    public Mono<Boolean> existBookAuthorByAuthorId(Integer authorId) {
        String sql = """				
                SELECT CASE
                            WHEN COUNT(ba.book_id) > 0 THEN true
                            ELSE false
                       END as result
                FROM book_authors AS ba
                WHERE ba.author_id = :authorId
                """;

        return this.databaseClient.sql(sql)
                .bind("authorId", authorId)
                .map((row, metadata) -> row.get("result", Boolean.class))
                .first();
    }

    @Override
    public Mono<IBookProjection> findAllBookAuthorByBookId(Integer bookId) {
        String sql = """				
                SELECT b.id AS id,
                        b.title AS title,
                        b.publication_date AS publicationDate,
                        b.online_availability AS onlineAvailability,
                        STRING_AGG(a.first_name||' '||a.last_name, ',') AS concatAuthors
                FROM book_authors AS ba
                    INNER JOIN books AS b ON(ba.book_id = b.id)
                    INNER JOIN authors AS a ON(ba.author_id = a.id)
                WHERE b.id = :bookId
                GROUP BY b.id, b.title, b.publication_date, b.online_availability
                """;

        return databaseClient.sql(sql)
                .bind("bookId", bookId)
                .map((row, metadata) -> {
                    log.info("Metadata de publicationDate: {} ", metadata.getColumnMetadata("publicationDate"));

                    return (IBookProjection) BookVO.builder()
                            .id(row.get("id", Integer.class))
                            .title(row.get("title", String.class))
                            .publicationDate(row.get("publicationDate", LocalDate.class))
                            .onlineAvailability(row.get("onlineAvailability", Boolean.class))
                            .concatAuthors(row.get("concatAuthors", String.class))
                            .build();
                })
                .first();
    }

    @Override
    public Mono<Void> deleteBookAuthorByBookId(Integer bookId) {
        String sql = """			
                DELETE FROM book_authors AS ba
                WHERE ba.book_id = :bookId
                """;

        return databaseClient.sql(sql)
                .bind("bookId", bookId)
                .fetch()
                .rowsUpdated()
                .then()
                .onErrorMap(t -> {
                    log.error(t.getMessage());
                    return new ApiException("Error in delete book_authors, bookId " + bookId, HttpStatus.NOT_FOUND);

                });
    }

    @Override
    public Mono<Void> deleteBookAuthorByAuthorId(Integer authorId) {
        String sql = """			
                DELETE FROM book_authors AS ba
                WHERE ba.author_id = :authorId
                """;

        return this.databaseClient.sql(sql)
                .bind("authorId", authorId)
                .fetch()
                .rowsUpdated()
                .then()
                .onErrorMap(t -> {
                    log.error("Ocurrió un error: " + t.getMessage());
                    return new ApiException("Error in delete book_authors, authorId " + authorId, HttpStatus.NOT_FOUND);
                });
    }

    @Override
    public Flux<IBookProjection> findAllToPage(BookCriteria bookCriteria, Pageable pageable) {
        String select = """				
                SELECT b.id as bookId, b.title as title, b.publication_date as publicationDate, b.online_availability as onlineAvailability,
                      STRING_AGG(a.first_name||' '||a.last_name, ', ') as concatAuthors
                """;

        String from = """		
                FROM book_authors ba
                    INNER JOIN books b ON ba.book_id = b.id
                    INNER JOIN authors a ON ba.author_id = a.id
                """;

        String where = "";

        StringBuilder sqlWhere = new StringBuilder();
        boolean flag = false;

        if (StringUtils.hasText(bookCriteria.q())) {

            if (flag) {
                sqlWhere.append("OR ");
            }

            sqlWhere.append("b.title LIKE :q ");
            flag = true;
        }

        if (bookCriteria.publicationDate() != null) {

            if (flag) {
                sqlWhere.append("OR ");
            }

            sqlWhere.append("b.publicationDate = :publicationDate ");
            flag = true;
        }

        if (flag) {
            where = sqlWhere.insert(0, "WHERE ").toString();
        }

        String limit = """
                GROUP BY b.id
                ORDER BY b.id ASC
                LIMIT :pageSize OFFSET :offset
                """;

        String sql = select + from + where + limit;
        log.info(sql);

        DatabaseClient.GenericExecuteSpec ges = databaseClient.sql(sql);

        if (StringUtils.hasText(bookCriteria.q())) {
            ges = ges.bind("q", "%" + bookCriteria.q() + "%");
        }

        if (bookCriteria.publicationDate() != null) {
            ges = ges.bind("publicationDate", bookCriteria.publicationDate());
        }

        return ges.bind("pageSize", pageable.getPageSize())
                .bind("offset", pageable.getOffset())
                .map((row, metadata) -> {
                    log.info("publicationDate {} ", metadata.getColumnMetadata("publicationDate"));
                    log.info("bookId {} ", metadata.getColumnMetadata("bookId").toString());

                    return (IBookProjection) BookVO.builder()
                            .id(row.get("bookId", Integer.class))
                            .title(row.get("title", String.class))
                            .publicationDate(row.get("publicationDate", LocalDate.class))
                            .onlineAvailability(row.get("onlineAvailability", Boolean.class))
                            .concatAuthors(row.get("concatAuthors", String.class))
                            .build();
                })
                .all();
    }
}
````

Del código anterior, analicemos el método `saveAllBookAuthor()`:

````java

@Override
public Mono<Void> saveAllBookAuthor(List<BookAuthor> bookAuthorList) {
    List<Mono<Long>> inserts = bookAuthorList.stream()
            .map(bookAuthorToSave -> this.databaseClient.sql("""
                            INSERT INTO book_authors(book_id, author_id)
                            VALUES(:bookId, :authorId)
                            """)
                    .bind("bookId", bookAuthorToSave.getBookId())
                    .bind("authorId", bookAuthorToSave.getAuthorId())
                    .fetch()
                    .rowsUpdated()
                    .onErrorMap(error -> new ApiException(error.getMessage(), HttpStatus.BAD_REQUEST))
            )
            .toList();

    Flux<Long> concat = Flux.concat(inserts);
    return concat.then();
}
````

**Donde**

- `Flux.concat(inserts)` toma una lista de `Mono<Long>` (en este caso, cada `Mono` representa una operación de inserción
  en la base de datos) y los concatena en un solo `Flux<Long>`.


- Esto significa que los `Mono<Long>` se ejecutarán uno tras otro en orden secuencial. El segundo `Mono` no comenzará a
  ejecutarse hasta que el primero haya completado su operación.


- A medida que cada `Mono<Long>` se completa (ya sea emitiendo un valor o un error), el `Flux` resultante emitirá esos
  valores. Sin embargo, en este caso, estás utilizando `then()` al final, por lo que no te interesa el resultado de los
  Long emitidos (el número de filas actualizadas), sino que deseas que el flujo termine correctamente.


- Si alguno de los `Mono<Long>` en la lista falla (es decir, si alguna inserción provoca un error), el `Flux` emitirá un
  error, y el flujo se detendrá. Esto es útil para manejar errores de manera centralizada: si hay un error en cualquiera
  de las inserciones, toda la operación de inserción se considera fallida.


- `Flux.concat()` es útil, ya que fuerza la suscripción y ejecución de cada `Mono` secuencialmente.

En resumen, `Flux.concat(inserts)` se utiliza aquí para gestionar las inserciones en orden y de forma secuencial,
mientras que la estructura reactiva permite manejar errores y completar la operación de manera eficiente.

A continuación se muestra la implementación del servicio `BookServiceImpl`.

````java

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements IBookService {

    private final IBookRepository bookRepository;
    private final IAuthorRepository authorRepository;
    private final IBookAuthorDao bookAuthorDao;
    private final BookMapper bookMapper;

    @Override
    @Transactional(readOnly = true)
    public Mono<Page<IBookProjection>> findAllToPage(BookCriteria bookCriteria, Pageable pageable) {
        Mono<Long> countBookAuthorByCriteria = this.bookAuthorDao.findCountBookAuthorByCriteria(bookCriteria);
        return this.bookAuthorDao.findAllToPage(bookCriteria, pageable)
                .collectList()
                .switchIfEmpty(Mono.error(new ApiException("Not result", HttpStatus.NO_CONTENT)))
                .zipWith(countBookAuthorByCriteria, (iBookProjections, total) -> new PageImpl<>(iBookProjections, pageable, total));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<IBookProjection> findBookById(Integer bookId) {
        return this.bookAuthorDao.findAllBookAuthorByBookId(bookId)
                .switchIfEmpty(Mono.error(new ApiException("No hay resultados con bookId: %d".formatted(bookId), HttpStatus.NOT_FOUND)));
    }

    @Override
    @Transactional
    public Mono<Integer> saveBook(RegisterBookDTO registerBookDTO) {
        Mono<Book> bookMonoToSave = this.bookMapper.toBook(registerBookDTO);
        Mono<Book> bookMonoDB = bookMonoToSave.flatMap(this.bookRepository::save);

        return this.authorRepository.findAllAuthorsByIdIn(registerBookDTO.getAuthors())
                .collectList()
                .flatMap(authors -> {
                    if (authors.size() != registerBookDTO.getAuthors().size()) {
                        return Mono.error(new ApiException("Algunos autores no existen en la BD", HttpStatus.BAD_REQUEST));
                    }
                    return Mono.just(authors);
                }).zipWith(bookMonoDB, (authors, bookDB) -> {
                    List<BookAuthor> bookAuthorList = this.bookMapper.toBookAuthorList(authors, bookDB.getId());
                    return this.bookAuthorDao.saveAllBookAuthor(bookAuthorList).then(Mono.just(bookDB.getId()));
                })
                .flatMap(bookIdMono -> bookIdMono);
    }

    @Override
    @Transactional
    public Mono<Boolean> deleteBook(Integer bookId) {
        return this.bookRepository.findById(bookId)
                .flatMap(bookDB -> this.bookAuthorDao.existBookAuthorByBookId(bookId))
                .flatMap(existsBookAuthor -> {
                    log.info("Existe el libro en la tabla book_authors?: {}", existsBookAuthor);
                    if (existsBookAuthor) {
                        return this.bookAuthorDao.deleteBookAuthorByBookId(bookId).then(Mono.just(true));
                    }
                    return Mono.just(true);
                })
                .flatMap(canContinue -> this.bookRepository.deleteById(bookId).then(Mono.just(true)))
                .switchIfEmpty(Mono.error(new ApiException("No se encontró el libro con id %s para eliminar".formatted(bookId), HttpStatus.NOT_FOUND)));
    }
}
````

Observemos que la clase de servicio anterior utiliza un `BookMapper`, así que es lo que se muestra a continuación:

````java

@Slf4j
@RequiredArgsConstructor
@Component
public class BookMapper {

    private final ModelMapper modelMapper;

    public Mono<Book> toBook(RegisterBookDTO dto) {
        try {
            Book book = modelMapper.map(dto, Book.class);
            return Mono.just(book);
        } catch (Exception e) {
            log.error("Error en mapeo para registrar book:: {}", e.getMessage());
            return Mono.error(new ApiException("Error al mapear entidad Author", HttpStatus.BAD_REQUEST));
        }
    }

    public List<BookAuthor> toBookAuthorList(List<Author> authors, Integer bookId) {
        return authors.stream()
                .map(author -> BookAuthor
                        .builder()
                        .bookId(bookId)
                        .authorId(author.getId())
                        .build()
                )
                .toList();
    }
}
````

## Creando el controller del Book

````java

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/books")
public class BookRestController {

    private final IBookService bookService;

    @GetMapping(path = "/pages")
    public Mono<ResponseEntity<Page<IBookProjection>>> findAllPage(@RequestParam(name = "q", defaultValue = "", required = false) String q,
                                                                   @RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                                   @RequestParam(name = "size", defaultValue = "5", required = false) int size,
                                                                   @RequestParam(name = "sortBy", defaultValue = "bookId", required = false) String sortBy,
                                                                   @RequestParam(name = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
                                                                   @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate publicationDate) {

        String[] sortArray = sortBy.contains(",") ?
                Arrays.stream(sortBy.split(",")).map(String::trim).toArray(String[]::new) :
                new String[]{sortBy.trim()};

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortArray);
        Pageable pageable = PageRequest.of(page, size, sort);
        BookCriteria bookCriteria = new BookCriteria(q, publicationDate);

        return this.bookService.findAllToPage(bookCriteria, pageable)
                .flatMap(bookProjections -> Mono.just(ResponseEntity.ok(bookProjections)));
    }

    @GetMapping(path = "/{bookId}")
    public Mono<ResponseEntity<IBookProjection>> getBook(@PathVariable Integer bookId) {
        return this.bookService.findBookById(bookId)
                .flatMap(bookProjection -> Mono.just(ResponseEntity.ok(bookProjection)));
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> registerBook(@RequestBody RegisterBookDTO registerBookDTO) {
        return this.bookService.saveBook(registerBookDTO)
                .doOnNext(bookId -> log.info("bookId: {}", bookId))
                .flatMap(bookId -> Mono.just(new ResponseEntity<>(HttpStatus.CREATED)));
    }

    @DeleteMapping(path = "/{bookId}")
    public Mono<ResponseEntity<Void>> deleteBook(@PathVariable Integer bookId) {
        return this.bookService.deleteBook(bookId)
                .map(wasDeleted -> ResponseEntity.noContent().build());
    }
}
````

## Creando un Value Object (VO)

Crearemos el `value object (VO)` que agrupará las características de un `Book` y quien además implementará la interfaz
de proyección `IBookProjection`:

````java

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@ToString
public class BookVO implements IBookProjection {
    private Integer id;
    private String title;
    private LocalDate publicationDate;
    private String concatAuthors;
    private Boolean onlineAvailability;

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public LocalDate getPublicationDate() {
        return this.publicationDate;
    }

    @Override
    public Boolean getOnlineAvailability() {
        return this.onlineAvailability;
    }

    @Override
    public String getConcatAuthors() {
        return this.concatAuthors;
    }
}
````

## Crea controlador de Author

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/authors")
public class AuthorRestController {

    private final IAuthorService authorService;

    @GetMapping
    public Mono<ResponseEntity<Flux<Author>>> findAllAuthors(AuthorCriteria authorCriteria) {
        Flux<Author> authorFlux = this.authorService.findAll(authorCriteria);
        return authorFlux
                .hasElements()
                .flatMap(hasElements -> {
                    if (hasElements) {
                        return Mono.just(ResponseEntity.ok(authorFlux));
                    }
                    return Mono.just(ResponseEntity.noContent().build());
                });
    }

    @GetMapping(path = "/pages")
    public Mono<ResponseEntity<Page<IAuthorProjection>>> findAllPage(@RequestParam(name = "query", defaultValue = "", required = false) String query,
                                                                     @RequestParam(name = "page", defaultValue = "0", required = false) int pageNumber,
                                                                     @RequestParam(name = "size", defaultValue = "5", required = false) int pageSize) {
        return this.authorService.findAllToPage(query, pageNumber, pageSize)
                .map(ResponseEntity::ok);
    }

    @GetMapping(path = "/{authorId}")
    public Mono<ResponseEntity<IAuthorProjection>> getAuthor(@PathVariable Integer authorId) throws ApiException {
        return this.authorService.findAuthorById(authorId)
                .flatMap(authorProjection -> Mono.just(ResponseEntity.ok(authorProjection)));
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> registerAuthor(@RequestBody RegisterAuthorDTO registerAuthorDTO) throws ApiException {
        return this.authorService.saveAuthor(registerAuthorDTO)
                .flatMap(affectedRows -> Mono.just(new ResponseEntity<>(HttpStatus.CREATED)));
    }

    @PutMapping(path = "/{authorId}")
    public Mono<ResponseEntity<ResponseMessage<IAuthorProjection>>> updateAuthor(@PathVariable Integer authorId,
                                                                                 @RequestBody UpdateAuthorDTO updateAuthorDTO) throws ApiException {
        return this.authorService.updateAuthor(authorId, updateAuthorDTO)
                .flatMap(authorProjection -> Mono.just(ResponseMessage.<IAuthorProjection>builder()
                        .message("Registro modificado")
                        .content(authorProjection)
                        .build())
                )
                .flatMap(msg -> Mono.just(new ResponseEntity<>(msg, HttpStatus.OK)));
    }

    @DeleteMapping(path = "/{authorId}")
    public Mono<ResponseEntity<Void>> deleteAuthor(@PathVariable Integer authorId) throws ApiException {
        return this.authorService.deleteAuthor(authorId)
                .map(wasDeleted -> ResponseEntity.noContent().build());
    }
}
````
