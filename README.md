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
      - 5433
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
DROP TABLE IF EXISTS book_authors;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS authors;

CREATE TABLE authors(
    id SERIAL,
    first_name VARCHAR(45) NOT NULL,
    last_name VARCHAR(45) NOT NULL,
    birthdate DATE NOT NULL,
    CONSTRAINT pk_authors PRIMARY KEY(id)
);

CREATE TABLE books(
    id SERIAL,
    title VARCHAR(255) NOT NULL,
    publication_date DATE NOT NULL,
    online_availability BOOLEAN DEFAULT FALSE,
    CONSTRAINT pk_books PRIMARY KEY(id)
);

CREATE TABLE book_authors(
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
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
        return initializer;
    }
}
````

> Si hasta este punto ejecutamos la aplicación, veremos que la ejecución es exitosa y las tablas se crean correctamente.

## Definiendo Modelo de Datos

Crearemos los modelos de datos correspondiente a las tablas que definimos en el `schema.sql`. Estas clases serán creadas
en el directorio `/persistence/entity`:

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
> es que no tenga una clave primaria, sino más bien solo tenga las claves foráneas correspondiente a las tablas
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
    @Query(value = """
            INSERT INTO authors(first_name, last_name, birthdate)
            VALUES(:#{#author.firstName}, :#{#author.lastName}, :#{#author.birthdate})
            """)
    Mono<Integer> saveAuthor(@Param(value = "author") Author author);

    @Query(value = """
            UPDATE authors
            SET first_name = :#{#author.firstName},
                last_name = :#{#author.lastName},
                birthdate = :#{#author.birthdate}
            WHERE id = :#{#author.id}
            """)
    Mono<Integer> updateAuthor(@Param(value = "author") Author author);

    @Query("""
            SELECT COUNT(a.id)
            FROM authors AS a
            WHERE a.first_name LIKE '%' || :q || '%' OR a.last_name LIKE '%' || :q || '%'
            """)
    Mono<Integer> findCountByQ(@Param(value = "q") String q);

    @Query("""
            SELECT a.id, a.first_name, a.last_name, CONCAT(a.first_name, ' ', a.last_name) AS full_name, a.birthdate
            FROM authors AS a
            WHERE a.id = :authorId
            """)
    Mono<IAuthorProjection> findByAuthorId(@Param(value = "authorId") Integer authorId);

    @Query("""
            SELECT a.id, a.first_name, a.last_name, a.first_name || ' ' || a.last_name AS full_name, a.birthdate
            FROM authors AS a
            WHERE a.first_name LIKE CONCAT('%',:q,'%') OR a.last_name LIKE CONCAT('%',:q,'%')
            ORDER BY a.id ASC
            LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
            """)
    Flux<IAuthorProjection> findByQ(@Param(value = "q") String q, @Param(value = "pageable") Pageable pageable);
}
````

En la interfaz anterior hemos definido métodos personalizados, donde:

- Usamos la anotación `@Query()` para definir nuestra consulta.
- La consulta usada en la anotación `@Query()` es `SQL nativo`, ya que estamos trabajando con `Spring Data R2DBC`
  y no con `Spring Data JPA`, aunque dicho sea de paso, con Spring Data JPA también se puede trabajar con `SQL nativo`
  solo que en ese caso es necesario agregar el atributo `nativeQuery` en la anotación de la siguiente manera
  `@Query(value = "TU_CONSULTA_SQL", nativeQuery = true)`, mientras que con `Spring Data R2DBC` usamos
  la consulta SQL directamente en la anotación.
- En `Spring Data JPA` cuando definimos una consulta personalizada, a través de la anotación `@Query()`, que modifica
  los registros de la base de datos, ya sea usando JPQL o SQL nativo; consultas como `INSERT, UPDATE o DELETE` es
  necesario agregar al método personalizado la anotación `@Modifying`. En nuestro caso, como estamos usando
  `Spring Data R2DBC`, no es necesario agregar dicha anotación, por eso es que nuestros métodos `saveAuthor()` y
  `updateAuthor()` carecen de él.
- Normalmente, cuando definimos parámetros a nuestros métodos de repositorio, si son pocos parámetros podemos definirlos
  uno a uno, pero si son muchos parámetros, podemos pasarle directamente un objeto que tendrá las propiedades que
  usaremos en la consulta. En nuestro caso, observemos la firma de nuestro método saveAuthor()
  `Mono<Integer> saveAuthor(@Param(value = "author") Author author)`, le estamos pasando la clase Author.
- Para usar las propiedades del objeto pasado por parámetro dentro de la consulta SQL usamos la siguiente expresión:
  `:#{#author.firstName}`, donde `author` es el parámetro definido en el método y `firstName` es la propiedad del
  objeto.

Algo importante que se debe resaltar en las consultas personalizadas del repositorio anterior es que en los métodos
`findByAuthorId` y `findByQ` estamos usando el concepto de `Projections` (a modo de ejemplo), con
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
        return Arrays.asList(getConcatAuthors().split(", "));
    }
}
````

## Creando DTOs

A continuación mostramos todos los dtos creados usando `record`. Los primeros records creados son para las consultas
que realizaremos usando `criteria`, es decir consultas que serán elaboradas de manera dinámica:

````java
public record AuthorCriteria(String firstName, Boolean lastName) {
}
````

````java
public record AuthorFilter(String q) {
}
````

````java
public record BookCriteria(String q, LocalDate publicationDate) {
}
````

````java
public record RegisterAuthorDTO(String firstName,
                                String lastName,
                                @JsonFormat(pattern = "dd/MM/yyyy") LocalDate birthdate) {
}
````

En el siguiente `record` se ha definido un `constructor compacto` para poder darle un valor por defecto al atributo
`onlineAvailability` cuando se cree el objeto del record con valor `null` para dicho atributo:

````java
public record RegisterBookDTO(String title,
                              @JsonFormat(pattern = "dd/MM/yyyy") LocalDate publicationDate,
                              List<Integer> authors,
                              Boolean onlineAvailability) {
    // Constructor compacto
    // Las asignaciones se realizan de manera automática
    public RegisterBookDTO {
        onlineAvailability = onlineAvailability != null && onlineAvailability;
    }
}
````

````java
public record UpdateAuthorDTO(String firstName,
                              String lastName,
                              @JsonFormat(pattern = "dd/MM/yyyy") LocalDate birthdate) {
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


