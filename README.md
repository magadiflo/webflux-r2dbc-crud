# [WebFlux R2DBC Crud using PostgreSQL](https://www.youtube.com/watch?v=s6qKE0FD3BU&t=2137s)

- Proyecto tomado del canal de youtube de `Joas Dev`.
- Este proyecto está actualizado al `25/06/2025` con algunos detalles que se vieron en el curso de
  [java-reactive-programming](https://github.com/magadiflo/java-reactive-programming.git) y de
  [webFlux-masterclass-microservices](https://github.com/magadiflo/webFlux-masterclass-microservices.git).

---

## Dependencias

A continuación se muestran las dependencias utilizadas en este proyecto, de las cuales, la única dependencia que
agregamos manualmente fue [MapStruct](https://mapstruct.org/), las demás dependencias las agregamos desde
[Spring Initializr (ver dependencias)](https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.5.3&packaging=jar&jvmVersion=21&groupId=dev.magadiflo&artifactId=webflux-r2dbc-crud&name=webflux-r2dbc-crud&description=Demo%20project%20for%20Spring%20WebFlux%20with%20r2dbc%20PostgreSQL&packageName=dev.magadiflo.r2dbc.app&dependencies=webflux,data-r2dbc,postgresql,lombok).

````xml
<!--Spring Boot 3.5.3-->
<!--Java 21-->
<!--org.mapstruct.version 1.6.3-->
<!--lombok-mapstruct-binding.version 0.2.0-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-r2dbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <!--Agregado manualmente-->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${org.mapstruct.version}</version>
    </dependency>
    <!--/Agregado manualmente-->
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

### Procesadores de anotaciones

- [Using MapStruct with Maven and Lombok.](https://bootify.io/spring-data/mapstruct-with-maven-and-lombok.html)
- [Using MapStruct With Lombok](https://www.baeldung.com/java-mapstruct-lombok)

Como vamos a trabajar con `MapStruct` necesitamos ampliar el `maven-compiler-plugin` para activar la generación de
código de `MapStruct`. Observar que nuestro primer procesador de anotaciones es `Lombok`, seguido directamente por
`MapStruct`. Se requiere otra referencia a `lombok-mapstruct-binding` para que estas dos bibliotecas funcionen juntas.
Sin `Lombok`, solo se necesitaría el `mapstruct-processor` en este momento.

````xml

<plugins>
    <!--MapStruct-->
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>${maven-compiler-plugin.version}</version>
        <configuration>
            <source>${java.version}</source>
            <target>${java.version}</target>
            <annotationProcessorPaths>
                <path>
                    <groupId>org.projectlombok</groupId>
                    <artifactId>lombok</artifactId>
                    <version>${lombok.version}</version>
                </path>
                <path>
                    <groupId>org.mapstruct</groupId>
                    <artifactId>mapstruct-processor</artifactId>
                    <version>${org.mapstruct.version}</version>
                </path>
                <path>
                    <groupId>org.projectlombok</groupId>
                    <artifactId>lombok-mapstruct-binding</artifactId>
                    <version>${lombok-mapstruct-binding.version}</version>
                </path>
            </annotationProcessorPaths>
        </configuration>
    </plugin>
    <!--/MapStruct-->
</plugins>
````

Es fácil cometer errores aquí, ya que los procesadores de anotaciones son una función avanzada. El principal error es
olvidar que nuestro entorno de ejecución buscará procesadores de anotaciones en el `path` o en el `classpath`, pero no
en ambas.

Debemos tener en cuenta que, a partir de la versión `1.18.16` de `Lombok` hacia arriba, necesitamos agregar tanto la
dependencia `lombok-mapstruct-binding` de `Lombok` como la dependencia `mapstruct-processor` en el elemento
`annotationProcessorPaths`. Si no lo hacemos, podríamos obtener un error de compilación:
`“Propiedad desconocida en el tipo de resultado…”`.

Necesitamos la dependencia `lombok-mapstruct-binding` para que `Lombok` y `MapStruct` funcionen juntos. En esencia,
le indica a `MapStruct` que espere hasta que `Lombok` haya completado todo el procesamiento de anotaciones antes
de generar clases de mapeador para los beans mejorados con `Lombok`.

## MapStruct vs ModelMapper

> En el proyecto original del tutorial de `Joas Dev` se usa como mapeador a `ModelMapper`, pero en mi caso, opté por
> usar `MapStruct` debido a las siguientes razones.

### ⚙️ MapStruct

- `Rendimiento superior`: Genera código de mapeo en tiempo de compilación, `evitando el uso de reflexión` y mejorando
  la velocidad.
- `Detección temprana de errores`: Al generar código en compilación, los errores de mapeo se detectan antes de ejecutar
  la aplicación.
- `Menor uso de memoria`: No usa reflection, lo que reduce la presión sobre el garbage collector.
- `Ideal para proyectos grandes o críticos en rendimiento`, como los que suelen construirse con `WebFlux`.
- `Más configuración inicial`, ya que debes definir interfaces y métodos de mapeo explícitamente.

### 🔄 ModelMapper

- `Más fácil de usar al principio`: Mapea automáticamente campos con nombres similares `usando reflexión`.
- Puede tener problemas con tipos genéricos complejos de los streams reactivos
- `Menor esfuerzo inicial`, útil para prototipos o proyectos pequeños.
- `Menor rendimiento`: La `reflexión` introduce una sobrecarga que puede ser significativa en aplicaciones reactivas.
- `Menos control` sobre el proceso de mapeo, lo que puede dificultar el mantenimiento a largo plazo.
- Los errores de configuración solo se detectan en runtime.
- Mayor consumo de memoria y CPU.

### ✅ Conclusión

Dado el enfoque en rendimiento, claridad y mantenimiento en sistemas reactivamente transaccionales, `MapStruct` encaja
mejor con el estilo y los objetivos. Además, al generar código explícito, facilita la documentación y el control del
flujo de datos.

## Creando base de datos

Creamos la base de datos `db_webflux_r2dbc` cuyo usuario es `postgres` y la contraseña es `magadiflo`.

![01.png](assets/01.png)

## Configurando base de datos

En el `application.yml` agregamos las siguientes configuraciones.

````yml
server:
  port: 8080
  error:
    include-message: always

spring:
  application:
    name: webflux-r2dbc-crud
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/db_webflux_r2dbc
    username: postgres
    password: magadiflo
````

La URL de conexión está configurada en `r2dbc:postgresql://localhost:5432/db_webflux_r2dbc`, donde:

- `r2dbc`, indicamos que usaremos `r2dbc` para conectarnos a la base de datos. Recordar que cuando usamos una
  aplicación tradicional como `Spring Boot MVC` (no reactiva) usamos `jdbc`.
- `db_webflux_r2dbc`, es el nombre de la base de datos.
- `5432`, es el puerto por defecto `PostgreSQL`.

Las propiedades `spring.r2dbc.username` y `spring.r2dbc.password` proporcionan las credenciales para conectarse a
la base de datos.

## Habilitando logging para ver los queries y parameters en las consultas a PostgreSQL

En el `application.yml` agregamos las siguientes configuraciones para poder observar qué instrucciones sql se están
ejecutando y qué parámetros se están enviando.

````yml
logging:
  level:
    io.r2dbc.postgresql.QUERY: DEBUG
    io.r2dbc.postgresql.PARAM: DEBUG
````

✨ ¿Qué hace cada línea?

- `logging.level`, define el nivel de detalle de los logs que Spring Boot va a registrar en la consola o en el archivo
  de logs, por cada paquete o clase.
- `io.r2dbc.postgresql.QUERY: DEBUG`, este ajuste activa el registro en nivel DEBUG de todas las consultas SQL que se
  ejecutan a través del driver `R2DBC PostgreSQL`.
    - Te permite ver qué sentencias SQL se envían a la base de datos de manera reactiva.
    - Ejemplo de log que verás:
        ````bash
        DEBUG 17820 --- io.r2dbc.postgresql.QUERY: Executing query: INSERT INTO authors (first_name, last_name, birthdate) VALUES ($1, $2, $3) RETURNING id
        ````
- `io.r2dbc.postgresql.PARAM: DEBUG`, este ajuste muestra en nivel DEBUG los valores de los parámetros que se usan en
  las consultas preparadas.
    - Es útil para depurar y saber exactamente qué datos se están pasando en las variables.
    - Ejemplo de log que verás:
      ````bash
      DEBUG 17820 --- io.r2dbc.postgresql.PARAM: Bind parameter [0] to: Ale
      DEBUG 17820 --- io.r2dbc.postgresql.PARAM: Bind parameter [1] to: Flo
      DEBUG 17820 --- io.r2dbc.postgresql.PARAM: Bind parameter [2] to: 2025-05-06
      ````

## Creando el esquema y datos de nuestra base de datos

Creamos el siguiente directorio `src/main/resources/sql` en nuestro proyecto. Aquí creamos el archivo `scheme.sql`
donde definimos las tablas `authors`, `books` y su relación de muchos a muchos `book_authors`.

Definimos las instrucciones `DROP TABLE...` al inicio de este script para que la aplicación inicie limpia y siempre
con los datos iniciales del archivo `data.sql` que crearemos luego.

````sql
DROP TABLE IF EXISTS book_authors;
DROP TABLE IF EXISTS authors;
DROP TABLE IF EXISTS books;

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
    CONSTRAINT fk_authors_book_authors FOREIGN KEY(author_id) REFERENCES authors(id),
    CONSTRAINT pk_book_authors PRIMARY KEY(book_id, author_id)
);
````

Creamos el script `data.sql` en el mismo directorio que el `scheme.sql`. En este script `data.sql` vamos a definir
los valores iniciales con las que iniciará nuestra aplicación cada vez que sea ejecutada.

````sql
INSERT INTO authors(first_name, last_name, birthdate)
VALUES
('Milagros', 'Díaz', '2006-06-15'),
('Lesly', 'Águila', '1995-06-09'),
('Kiara', 'Lozano', '2001-10-03'),
('Briela', 'Cirilo', '1997-09-25');

INSERT INTO books(title, publication_date, online_availability)
VALUES
('Cien años de soledad', '1999-01-15', true),
('Paco Yunque', '1985-03-18', true),
('Los perros hambrientos', '2002-05-06', false),
('Edipo Rey', '1988-07-15', true);

-- Book 1: tiene como author a Milagros y Lesly
INSERT INTO book_authors(book_id, author_id)
VALUES
(1, 1),
(1, 2);

-- Book 2: tiene como author a Kiara
INSERT INTO book_authors(book_id, author_id)
VALUES
(2, 3);
````

## Inicializando Scheme y Data

Crearemos una clase de configuración en `dev/magadiflo/r2dbc/app/config/DatabaseInitConfig.java` donde definiremos un
`@Bean` que nos retornará un objeto del tipo `ConnectionFactoryInitializer`.

`Spring Data R2DBC` `ConnectionFactoryInitializer` proporciona una manera conveniente de configurar e inicializar una
fábrica de conexiones para una conexión de base de datos reactiva en una aplicación `Spring`. Escaneará el
`scheme.sql` y el `data.sql` en el `classpath` y ejecutará los `scripts SQL` para inicializar la base de datos cuando
la base de datos esté conectada.

````java

@Configuration
public class DatabaseInitConfig {
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ClassPathResource schema = new ClassPathResource("sql/scheme.sql");
        ClassPathResource data = new ClassPathResource("sql/data.sql");
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator(schema, data);

        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(resourceDatabasePopulator);
        return initializer;
    }
}
````

Si hasta este punto ejecutamos la aplicación, veremos que el log nos muestra una ejecución exitosa.

````bash
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.3)

2025-06-26T12:00:08.010-05:00  INFO 3520 --- [webflux-r2dbc-crud] [           main] d.m.r.app.WebfluxR2dbcCrudApplication    : Starting WebfluxR2dbcCrudApplication using Java 21.0.6 with PID 3520 (D:\programming\spring\02.youtube\10.joas_dev\webflux-r2dbc-crud\target\classes started by magadiflo in D:\programming\spring\02.youtube\10.joas_dev\webflux-r2dbc-crud)
2025-06-26T12:00:08.016-05:00  INFO 3520 --- [webflux-r2dbc-crud] [           main] d.m.r.app.WebfluxR2dbcCrudApplication    : No active profile set, falling back to 1 default profile: "default"
2025-06-26T12:00:09.038-05:00  INFO 3520 --- [webflux-r2dbc-crud] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data R2DBC repositories in DEFAULT mode.
2025-06-26T12:00:09.074-05:00  INFO 3520 --- [webflux-r2dbc-crud] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 18 ms. Found 0 R2DBC repository interfaces.
2025-06-26T12:00:10.703-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-26T12:00:10.714-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-26T12:00:10.787-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [      Thread-16] io.r2dbc.postgresql.QUERY                : Executing query: DROP TABLE IF EXISTS book_authors
2025-06-26T12:00:10.796-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-2] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-26T12:00:10.798-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-2] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-26T12:00:10.818-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: DROP TABLE IF EXISTS authors
2025-06-26T12:00:10.829-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: DROP TABLE IF EXISTS books
2025-06-26T12:00:10.836-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: CREATE TABLE authors( id SERIAL, first_name VARCHAR(45) NOT NULL, last_name VARCHAR(45) NOT NULL, birthdate DATE NOT NULL, CONSTRAINT pk_authors PRIMARY KEY(id) )
2025-06-26T12:00:10.860-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-3] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-26T12:00:10.861-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-3] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-26T12:00:10.875-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: CREATE TABLE books( id SERIAL, title VARCHAR(255) NOT NULL, publication_date DATE NOT NULL, online_availability BOOLEAN DEFAULT FALSE, CONSTRAINT pk_books PRIMARY KEY(id) )
2025-06-26T12:00:10.882-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: CREATE TABLE book_authors( book_id INTEGER NOT NULL, author_id INTEGER NOT NULL, CONSTRAINT fk_books_book_authors FOREIGN KEY(book_id) REFERENCES books(id), CONSTRAINT fk_authors_book_authors FOREIGN KEY(author_id) REFERENCES authors(id), CONSTRAINT pk_book_authors PRIMARY KEY(book_id, author_id) )
2025-06-26T12:00:10.894-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [      Thread-17] io.r2dbc.postgresql.QUERY                : Executing query: INSERT INTO authors(first_name, last_name, birthdate) VALUES ('Milagros', 'Díaz', '2006-06-15'), ('Lesly', 'Águila', '1995-06-09'), ('Kiara', 'Lozano', '2001-10-03'), ('Briela', 'Cirilo', '1997-09-25')
2025-06-26T12:00:10.897-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: INSERT INTO books(title, publication_date, online_availability) VALUES ('Cien años de soledad', '1999-01-15', true), ('Paco Yunque', '1985-03-18', true), ('Los perros hambrientos', '2002-05-06', false), ('Edipo Rey', '1988-07-15', true)
2025-06-26T12:00:10.900-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: INSERT INTO book_authors(book_id, author_id) VALUES (1, 1), (1, 2)
2025-06-26T12:00:10.903-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: INSERT INTO book_authors(book_id, author_id) VALUES (2, 3)
2025-06-26T12:00:10.948-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-4] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-26T12:00:10.950-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-4] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-26T12:00:11.055-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-5] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-26T12:00:11.057-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-5] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-26T12:00:11.128-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-6] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-26T12:00:11.130-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-6] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-26T12:00:11.237-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-7] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-26T12:00:11.239-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-7] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-26T12:00:11.343-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-8] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-26T12:00:11.344-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-8] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-26T12:00:11.432-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-26T12:00:11.435-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-26T12:00:11.546-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-2] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-26T12:00:11.548-05:00 DEBUG 3520 --- [webflux-r2dbc-crud] [actor-tcp-nio-2] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-26T12:00:11.893-05:00  INFO 3520 --- [webflux-r2dbc-crud] [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 8080 (http)
2025-06-26T12:00:11.919-05:00  INFO 3520 --- [webflux-r2dbc-crud] [           main] d.m.r.app.WebfluxR2dbcCrudApplication    : Started WebfluxR2dbcCrudApplication in 4.713 seconds (process running for 6.126)
````

Y si ahora revisamos la base de datos `db_webflux_r2dbc` en postgres, veremos que las tablas se han creado
y poblado correctamente.

![02.png](assets/02.png)

## Creando entidades: Author, Book y BookAuthor

Creamos las entidades correspondientes a las tablas que definimos en el `scheme.sql`. Es importante recordar que aquí
estamos trabajando con `R2DBC`, por lo tanto, hay que tener algunas consideraciones:

- No tenemos la anotación `@Entity` en `R2DBC`.
- Las anotaciones `@Table` o `@Column` realmente no son necesarios, pero si necesitamos agregar alguna personalización
  podemos usarlos. Por ejemplo, la anotación `@Table` nos permite redefinir el nombre de la tabla de la base de datos
  para cada entidad.
- La anotación `@Id` es necesaria para identificar la clave primaria de la entidad.
- A diferencia de `JPA`, `R2DBC` no maneja automáticamente las relaciones entre entidades (no hay `@OneToMany`,
  `@ManyToOne`, etc.).

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "authors")
public class Author {
    @Id
    private Integer id;
    private String firstName;
    private String lastName;
    private LocalDate birthdate;
}
````

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "books")
public class Book {
    @Id
    private Integer id;
    private String title;
    private LocalDate publicationDate;
    private Boolean onlineAvailability;
}
````

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "book_authors")
public class BookAuthor {
    private Integer bookId;
    private Integer authorId;
}
````

### ⚠️ Nota sobre Relaciones Muchos a Muchos en R2DBC

La tabla `book_authors` representa una **relación muchos a muchos** entre `Author` y `Book`:

- Un autor puede escribir muchos libros
- Un libro puede ser escrito por muchos autores

### Diseño de Base de Datos

Para esta relación `M:N`, hemos implementado una **clave primaria compuesta** que sigue el modelo relacional puro:

```sql
CREATE TABLE book_authors(
    book_id INTEGER NOT NULL,
    author_id INTEGER NOT NULL,
    CONSTRAINT fk_books_book_authors FOREIGN KEY(book_id) REFERENCES books(id),
    CONSTRAINT fk_authors_book_authors FOREIGN KEY(author_id) REFERENCES authors(id),
    CONSTRAINT pk_book_authors PRIMARY KEY(book_id, author_id) --Clave primaria compuesta
);
```

Este diseño:

- ✅ `Garantiza unicidad`: No permite relaciones duplicadas.
- ✅ `Es semánticamente correcto`: La PK representa exactamente la relación.
- ✅ `Sigue el modelo relacional puro`: Sin campos artificiales innecesarios.

### Limitación de R2DBC

`Spring Data R2DBC no soporta claves primarias compuestas` (a diferencia de JPA que tiene `@IdClass` y `@EmbeddedId`).
Por esta razón:

1. No podemos usar `ReactiveCrudRepository` para esta entidad.
2. Debemos usar `DatabaseClient` con `SQL nativo` para las operaciones `CRUD`.
3. Creamos una entidad `BookAuthor` `sin anotación @Id` para representar la tabla.

Esta aproximación nos permite mantener el diseño teóricamente correcto en base de datos mientras trabajamos con las
limitaciones actuales de `R2DBC`.

### ⚠️ Importante

> Si quisiéramos utilizar `ReactiveCrudRepository` con la entidad `BookAuthor`, sería necesario agregar una columna
> adicional en la tabla `book_authors`, como un identificador único (por ejemplo, `id SERIAL PRIMARY KEY`).
> Esta nueva columna actuaría como clave primaria y permitiría anotar un atributo correspondiente en la entidad
> con `@Id`.
>
> Sin embargo, al introducir una clave artificial, la tabla dejaría de representar un modelo relacional
> puramente compuesto, rompiendo en cierta forma con la normalización clásica de una relación muchos a muchos.
>
> Este caso lo podemos ver en el proyecto
> [webFlux-masterclass-microservices](https://github.com/magadiflo/webFlux-masterclass-microservices/tree/main/projects/webflux-playground/src/main/java/dev/magadiflo/app/sec03/entity)

## 🧩 Proyección personalizada: `AuthorProjection`

`AuthorProjection` es una `proyección basada en interfaz`, utilizada para representar parcialmente los datos de un
autor. `Spring Data` permite usar interfaces con solo los getters necesarios, lo que permite consultar solo los campos
deseados sin cargar toda la entidad.

> Para saber más sobre proyecciones ir a este
> repositorio [spring-data-jpa-projections](https://github.com/magadiflo/spring-data-jpa-projections)

````java

@JsonPropertyOrder(value = {"firstName", "lastName", "fullName", "birthdate"})
public interface AuthorProjection {
    String getFirstName();

    String getLastName();

    LocalDate getBirthdate();

    default String getFullName() {
        if (Objects.isNull(getFirstName()) || Objects.isNull(getLastName())) {
            return "";
        }
        return "%s %s".formatted(getFirstName(), getLastName());
    }
}
````

Esta interfaz utiliza la anotación `@JsonPropertyOrder(value = {"firstName", "lastName", "fullName", "birthdate"})`
para ordenar explícitamente los campos en la respuesta JSON en el orden indicado.

Sin esta anotación:

- Jackson no garantiza el orden de los campos.
- El orden puede cambiar entre ejecuciones, versiones de Spring, o versiones de Jackson.
- Campos default como fullName suelen aparecer en posiciones impredecibles.

Con esta anotación:

- La respuesta JSON mantiene siempre un orden estable.
- Se mejora la claridad para clientes y frontends.
- Se facilita la validación en tests automatizados.

### ✨ Nota importante

> La anotación `@JsonPropertyOrder` solo es necesaria en `interfaces` de proyección, ya que en estas el orden de las
> propiedades no está garantizado por defecto y puede variar.
>
> Si en lugar de una interfaz usamos un `record` o una `clase` DTO convencional, no necesitamos esta anotación, ya que
> en un `record`, el orden de los campos en el JSON coincide con el orden en que se definen los componentes del record.
> En una clase, Jackson respeta el orden en que declares los atributos.

## 🧩 Proyección personalizada: `BookProjection`

`BookProjection` es una proyección personalizada utilizada para representar la vista combinada de un libro junto con
sus autores, generalmente como resultado de una consulta `SQL con JOIN` y `STRING_AGG`.

- El campo `authors` contiene una cadena con los nombres de los autores concatenados (por ejemplo: "`Alice Smith`,
  `Bob Johnson`"), generada por la base de datos.
- Este campo es interno y se marca con `@JsonIgnore` para que `no se exponga` directamente en la `respuesta JSON`.
- En su lugar, se expone el método `authorNames()`, anotado con `@JsonProperty`, que transforma esa cadena en una lista
  de nombres individuales (`List<String>`).

````java
public record BookProjection(String title,
                             LocalDate publicationDate,
                             Boolean onlineAvailability,
                             @JsonIgnore
                             String authors) {

    @JsonProperty
    public List<String> authorNames() {
        if (Objects.isNull(this.authors) || this.authors.isBlank()) {
            return List.of();
        }
        return Arrays.stream(this.authors.split(","))
                .map(String::trim)
                .toList();
    }
}
````

## Creando repositorios

A nuestras entidades `Author` y `Book` les crearemos a cada uno su interfaz de repositorio. Estos repositorios nos
permitirán interactuar con las tablas de la base de datos `authors` y `books`. Con respecto a la entidad `BookAuthor`,
esta la manejaremos dentro de una clase `dao` haciendo uso del `DatabaseClient`.

La interfaz `ReactiveCrudRepository` nos permitirá usar sus métodos ya definidos, tales como el `save()`, `findById()`,
`findAll()`, `count()`, `delete()`, `deleteById()`, `deleteAll()`, etc.`

A continuación se muestra la creación del repositorio `BookRepository` para la entidad `Book`.

````java
public interface BookRepository extends ReactiveCrudRepository<Book, Integer> {
}
````

Otro repositorio que creamos es `AuthorRepository`. Al igual que otros repositorios en este proyecto, extiende de
`ReactiveCrudRepository`, lo que significa que ya incluye una serie de métodos predefinidos como `save()`, `findById()`,
`deleteById()`, entre otros.

> 🧪 Sin embargo, en este caso particular decidimos definir nuestros propios métodos personalizados, con el objetivo de
> practicar distintas formas de interactuar con la base de datos de manera reactiva.
>
> Esto no solo nos permite comprender mejor cómo funciona la capa de acceso a datos, sino que también puede ser útil en
> escenarios donde se requiera mayor control sobre la consulta o comportamiento específico.

````java
public interface AuthorRepository extends ReactiveCrudRepository<Author, Integer> {
    /**
     * @param author entity
     * @return affectedRows
     */
    @Modifying
    @Query(value = """
            INSERT INTO authors(first_name, last_name, birthdate)
            VALUES(:#{#author.firstName}, :#{#author.lastName}, :#{#author.birthdate})
            """)
    Mono<Integer> saveAuthor(@Param("author") Author author);

    /**
     * @param author entity
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
    Mono<Integer> updateAuthor(Author author);

    @Query("""
            SELECT a.id, a.first_name, a.last_name, a.birthdate
            FROM authors AS a
            WHERE a.id IN(:authorIds)
            """)
    Flux<Author> findAllAuthorsByIdIn(List<Integer> authorIds);

    @Query("""
            SELECT COUNT(a.id)
            FROM authors AS a
            WHERE a.first_name LIKE :#{'%' + #query + '%'}
                OR a.last_name LIKE :#{'%' + #query + '%'}
            """)
    Mono<Long> findCountByQuery(String query);

    @Query("""
            SELECT a.first_name, a.last_name, a.birthdate
            FROM authors AS a
            WHERE a.id = :authorId
            """)
    Mono<AuthorProjection> findAuthorById(Integer authorId);

    @Query("""
            SELECT a.first_name, a.last_name, a.birthdate
            FROM authors AS a
            WHERE a.first_name LIKE :#{'%' + #query + '%'}
                OR a.last_name LIKE :#{'%' + #query + '%'}
            ORDER BY a.id ASC
            LIMIT :#{#pageable.getPageSize()}
            OFFSET :#{#pageable.getOffset()}
            """)
    Flux<AuthorProjection> findByQuery(String query, Pageable pageable);

    @Modifying
    @Query("DELETE FROM authors AS a WHERE a.id = :authorId")
    Mono<Boolean> deleteAuthorById(Integer authorId);
}
````

En el repositorio `AuthorRepository` hemos definido métodos personalizados, donde:

- Usamos la anotación `@Query()` para definir nuestra consulta.


- La consulta usada en la anotación `@Query()` es `SQL nativo`, ya que estamos trabajando con `Spring Data R2DBC`
  y no con `Spring Data JPA`. Aunque dicho sea de paso, con `Spring Data JPA` también se puede trabajar con `SQL nativo`
  solo que en ese caso es necesario agregar el atributo `nativeQuery` en la anotación de la siguiente manera
  `@Query(value = "TU_CONSULTA_SQL_CON_JPA", nativeQuery = true)`, mientras que con `Spring Data R2DBC` usamos
  directamente `SQL nativo` en la anotación `@Query`.


- La anotación `@Modifying` indica que un método de consulta debe considerarse una consulta de modificación que puede
  devolver:
    - `Void` para descartar el recuento de actualizaciones y esperar a que se complete.
    - `Integer` u otro tipo numérico que emite el recuento de filas afectadas.
    - `Boolean` para indicar si se actualizó al menos una fila.

  Los métodos de consulta anotados con `@Modifying` suelen ser instrucciones `INSERT, UPDATE, DELETE y DDL` que no
  devuelven resultados tabulares.


- Normalmente, cuando definimos parámetros a nuestros métodos de repositorio, si son pocos parámetros podemos definirlos
  uno a uno, pero si son muchos parámetros, podemos pasarle directamente un objeto que tendrá las propiedades que
  usaremos en la consulta. En nuestro caso, observemos la firma de nuestro método `saveAuthor()`
  `Mono<Integer> saveAuthor(@Param("author") Author author)`, le estamos pasando la clase `Author`.


- Para usar las propiedades del objeto pasado por parámetro dentro de la consulta SQL usamos `SpEL`, por ejemplo:
  `:#{#author.firstName}`, donde `author` es el parámetro definido en el método y `firstName` es la propiedad del
  objeto. Esta sintaxis se utiliza para acceder a expresiones `SpEL (Spring Expression Language)`. Permite referenciar
  propiedades y métodos de objetos directamente en la consulta.


- El prefijo `#{}` indica que se está utilizando `SpEL`, y el símbolo `#` se utiliza para acceder a los parámetros del
  método o a las propiedades del objeto. Por ejemplo. `:#{#pageable.getPageSize()}` accede al método `getPageSize()` del
  objeto `Pageable` pasado como parámetro.

Veamos un poco más a detalle la siguiente consulta que hemos creado anteriormente.

````java

@Query("""
        SELECT a.first_name, a.last_name, a.birthdate
        FROM authors AS a
        WHERE a.first_name LIKE :#{'%' + #query + '%'}
            OR a.last_name LIKE :#{'%' + #query + '%'}
        ORDER BY a.id ASC
        LIMIT :#{#pageable.getPageSize()}
        OFFSET :#{#pageable.getOffset()}
        """)
Flux<AuthorProjection> findByQuery(String query, Pageable pageable);
````

Estamos pasando por parámetro un `String query` y un `Pageable pageable`. Centrémonos en el objeto `pageable`. Estamos
agregando este objeto `pageable` por parámetro con la única finalidad de poder usar los valores internos que nos
proporcione su implementación. En otras palabras, lo que pasamos por parámetro al método `findByQuery(...)` es la
variable `query` y la implementación de la interfaz `Pageable`. Esta implementación la podemos obtener de un
`PageRequest.of(pageNumber, pageSize)`. Internamente, la implementación hace ciertas operaciones, las mismas que
podemos obtenerlas, por ejemplo con el `getOffset()` que es la multiplicación del `pageNumber * pageSize`.

Por otro lado, algo importante que se debe resaltar en las consultas personalizadas del repositorio anterior es que en
los métodos `findAuthorById` y `findByQuery` estamos usando el concepto de `Projections` (a modo de ejemplo), con
`projections` podemos recuperar del total de columnas que tenga una tabla, solo las columnas que queramos. Por ejemplo,
si nuestra tabla tuviera 50 columnas, con projections podemos recuperar solo 5 columnas, no todas, sino las que son
realmente necesarias.

## 🧾 DTOs definidos con record

A continuación, se presentan los DTOs creados utilizando la palabra clave `record`. Algunos de ellos incluyen métodos
adicionales que nos ayudarán más adelante a evitar la duplicación de código y a mejorar la legibilidad en ciertas
operaciones lógicas.

Iniciamos con el primer record llamado `BookCriteria` quien va a actuar como un `DTO (Data Transfer Object)` que
encapsula los posibles criterios para realizar búsquedas o filtros relacionados con libros. Es útil, por ejemplo, al
implementar endpoints de búsqueda dinámica o filtros condicionales en un repositorio.

````java
public record BookCriteria(String query, LocalDate publicationDate) {
    public boolean hasQuery() {
        return Objects.nonNull(this.query) && !this.query.isBlank();
    }

    public boolean hasPublicationDate() {
        return Objects.nonNull(this.publicationDate);
    }
}
````

El siguiente `DTO (record)` llamado `BookRequest` representa la estructura de datos que se espera recibir al crear un
libro a través de una solicitud (por ejemplo, en un endpoint REST).

Incluye anotaciones de validación para asegurar la integridad de los datos entrantes, así como un `constructor compacto`
que transforma el valor de un campo opcional y un método auxiliar que facilita la lógica condicional.

````java
public record BookRequest(@NotBlank
                          @Size(min = 3)
                          String title,
                          @NotNull
                          LocalDate publicationDate,
                          Boolean onlineAvailability,
                          List<@NotNull Integer> authorIds) {
    // Constructor compacto
    public BookRequest {
        // El onlineAvailability es opcional. Si es null o false, será falso. Caso contrario será true
        // De esta manera se garantiza que siempre tenga un valor booleano definido y coherente
        onlineAvailability = Boolean.TRUE.equals(onlineAvailability);
    }

    public boolean hasNoAuthorIds() {
        return Objects.isNull(this.authorIds) || this.authorIds.isEmpty();
    }
}
````

✅ Consideraciones

- Se aprovechan las validaciones con `Jakarta Bean Validation` directamente en los campos del record.
- `List<@NotNull Integer> authorIds`, lista de identificadores de autores relacionados con el libro.
  Cada elemento de la lista es validado con `@NotNull`, lo que impide que haya elementos `nulos` dentro de la colección.
  Sin embargo, la lista en sí puede ser `null` o `vacía`, según lo definido en la lógica del método auxiliar
  `hasNoAuthorIds()`. Lo que no es válido es que algún elemento individual dentro de la lista sea `null`.
- El constructor compacto permite aplicar lógica limpia sin necesidad de crear una clase mutable.
- Se mantiene el código conciso, inmutable y seguro para su uso en entornos reactivos.

El siguiente `DTO` `BookUpdateRequest` representa la estructura de datos necesaria para actualizar la información de
un libro existente.

A diferencia de otros `DTOs` de entrada como `BookRequest`, todos los campos en `BookUpdateRequest` son obligatorios,
lo cual asegura que la operación de actualización sea completa y no parcial.

Se utilizan anotaciones de validación para garantizar que:

- El título sea válido y tenga una longitud mínima.
- La fecha de publicación y el estado de disponibilidad estén presentes.

Este de `DTO` será utilizado en operaciones `PUT`.

````java
public record BookUpdateRequest(@NotBlank
                                @Size(min = 3)
                                String title,
                                @NotNull
                                LocalDate publicationDate,
                                @NotNull
                                Boolean onlineAvailability) {
}
````

El siguiente `DTO` `BookAuthorUpdateRequest` representa la estructura esperada para actualizar la lista de autores
asociados a un libro.

Contiene un único campo: una lista de identificadores de autores (`authorIds`), en la cual cada elemento debe ser no
nulo, gracias a la anotación `@NotNull` aplicada sobre los elementos de la lista.

> ⚠️ Importante:
>
> La lista en sí (`authorIds`) puede ser `null` o `vacía` si así lo permite la lógica de negocio. Sin embargo, ningún
> elemento individual de la lista puede ser `null`, lo cual garantiza que los valores enviados sean identificadores
> válidos.

Este `DTO` es útil cuando se quiere actualizar solo los autores relacionados con un libro, manteniendo separados los
cambios estructurales (como título o fecha) de los cambios relacionales.

````java
public record BookAuthorUpdateRequest(List<@NotNull Integer> authorIds) {
}
````

A continuación se mostrarán otros `DTOs` creados para la aplicación.

````java
public record BookResponse(Integer id,
                           String title,
                           LocalDate publicationDate,
                           Boolean onlineAvailability) {
}
````

````java
public record AuthorRequest(String firstName,
                            String lastName,
                            LocalDate birthdate) {
}
````

````java
public record AuthorResponse(Integer id,
                             String firstName,
                             String lastName,
                             LocalDate birthdate) {
}
````

````java
public record AuthorCriteria(String firstName, String lastName) {
}
````

## 🛠️ DAO personalizado con `R2dbcEntityTemplate`: `AuthorDaoImpl`

Este `DAO` implementa una lógica de consulta dinámica para la entidad `Author`, utilizando `R2dbcEntityTemplate`,
una alternativa programática y orientada a objetos a las consultas `SQL manuales`.

> Su uso es comparable al `EntityManager` en `JPA`, pero en el contexto de `Spring Data R2DBC` y programación reactiva.
> Es decir en `JPA` utilizamos `EntityManager`
> [(spring-data-jpa-criteria-queries)](https://github.com/magadiflo/spring-data-jpa-criteria-queries/blob/main/src/main/java/dev/magadiflo/criteria_queries/persistence/dao/EmployeeSearchDao.java)
> mientras que aquí en `Spring Data R2DBC` y programación reactiva usamos `R2dbcEntityTemplate`.

````java
public interface AuthorDao {
    Flux<Author> findAuthorByCriteria(AuthorCriteria authorCriteria);
}
````

Define una operación de búsqueda que recibe un objeto `AuthorCriteria` con los posibles filtros (nombre y apellido del
autor).

````java
import org.springframework.data.relational.core.query.Criteria;

@RequiredArgsConstructor
@Repository
public class AuthorDaoImpl implements AuthorDao {

    private final R2dbcEntityTemplate template;

    @Override
    public Flux<Author> findAuthorByCriteria(AuthorCriteria authorCriteria) {
        Criteria criteria = Criteria.empty();

        if (Objects.nonNull(authorCriteria.firstName()) && !authorCriteria.firstName().trim().isEmpty()) {
            Criteria likeFirstName = Criteria.where("first_name").like(this.likePattern(authorCriteria.firstName()));
            criteria = criteria.and(likeFirstName);
        }

        if (Objects.nonNull(authorCriteria.lastName()) && !authorCriteria.lastName().trim().isEmpty()) {
            Criteria likeLastName = Criteria.where("last_name").like(this.likePattern(authorCriteria.lastName()));
            criteria = criteria.and(likeLastName);
        }

        Query query = Query.query(criteria);

        return this.template
                .select(Author.class)
                .matching(query)
                .all();
    }

    private String likePattern(String value) {
        return "%" + value.trim() + "%";
    }
}
````

### ⚙️ ¿Qué hace esta clase?

- Usa `R2dbcEntityTemplate` para construir y ejecutar consultas reactivas sin necesidad de escribir SQL.
- La lógica de filtrado es dinámica:
    - Si el `firstName` o `lastName` está presente en el `AuthorCriteria`, se aplica una cláusula `LIKE`.
    - Los filtros se combinan mediante `Criteria` (una clase propia de `Spring Data R2DBC`).
- Finalmente, se ejecuta la consulta con `.select(...).matching(query).all()` y se retorna un `Flux<Author>`.

### 💡 Ventajas del uso de `R2dbcEntityTemplate`

- Permite construir consultas más expresivas y legibles usando clases en lugar de strings SQL.
- Evita errores comunes de SQL al construir las queries de forma programática.
- Ideal para escenarios donde se requiere construir filtros condicionales y consultas dinámicas.
- Conserva el enfoque reactivo de extremo a extremo.

### ⚠️ Limitaciones de `R2dbcEntityTemplate`

Aunque `R2dbcEntityTemplate` facilita la construcción de consultas reactivas de manera programática, tiene algunas
limitaciones importantes:

- `Solo opera sobre una única entidad a la vez`: No permite realizar `joins` entre tablas directamente como lo harías
  con `SQL nativo` o con `DatabaseClient`. Esto significa que si necesitas combinar datos de varias entidades
  (por ejemplo, `Author` con `Book`), deberás:
    - Hacer múltiples consultas separadas.
    - O recurrir a `DatabaseClient` y construir una consulta SQL manual.


- `Falta de soporte para expresiones más complejas`: Las operaciones avanzadas como `subconsultas`, `GROUP BY`,
  `HAVING`, `funciones agregadas`, etc., están fuera del alcance del `API de Criteria`.

## 🛠️ DAO personalizado con `DatabaseClient`: `BookAuthorDaoImpl`

Este DAO implementa múltiples operaciones personalizadas para gestionar la relación entre `libros` y `autores`
(`book_authors`) utilizando `DatabaseClient`, un componente de bajo nivel en `Spring Data R2DBC` que permite ejecutar
consultas `SQL nativas` de forma reactiva.

> Podemos considerar a `DatabaseClient` como el equivalente reactivo de `JdbcTemplate`
> [(spring-data-jdbc-template-crud-api-rest):](https://github.com/magadiflo/spring-data-jdbc-template-crud-api-rest/blob/main/src/main/java/com/magadiflo/jdbc/template/app/repository/impl/UserRepositoryImpl.java)
> - En ambos casos se utiliza SQL nativo directamente para construir y ejecutar consultas.
> - Tienes control total sobre la estructura de la consulta (SELECT, JOIN, GROUP BY, etc.).
> - Los parámetros se enlazan de forma segura (:paramName en DatabaseClient, ? o :param en JdbcTemplate).
> - Eres responsable de mapear manualmente los resultados (`RowMapper en JdbcTemplate`,
    `Row + RowMetadata en DatabaseClient`).

````java
public interface BookAuthorDao {
    Mono<Long> countBookAuthorByCriteria(BookCriteria bookCriteria);

    Mono<Long> saveBookAuthor(BookAuthor bookAuthor);

    Mono<Void> saveAllBookAuthor(List<BookAuthor> bookAuthorList);

    Mono<Boolean> existBookAuthorByBookId(Integer bookId);

    Mono<Boolean> existBookAuthorByAuthorId(Integer authorId);

    Mono<Void> deleteBookAuthorByBookId(Integer bookId);

    Mono<Void> deleteBookAuthorByAuthorId(Integer authorId);

    Mono<BookProjection> findBookWithTheirAuthorsByBookId(Integer bookId);

    Flux<BookProjection> findAllToPage(BookCriteria bookCriteria, Pageable pageable);
}
````

### ⚙️ ¿Qué es DatabaseClient y por qué se usa?

`DatabaseClient` proporciona una interfaz programática, flexible y orientada a SQL para ejecutar consultas directamente
sobre la base de datos. Se utiliza en este DAO porque:

- Permite escribir consultas SQL completamente personalizadas, incluyendo:
    - Joins entre múltiples tablas.
    - Uso de funciones agregadas como STRING_AGG.
    - Subconsultas o estructuras complejas.
    - Paginación (LIMIT, OFFSET).

- Soporta binding de parámetros nombrados (`:param`) para prevenir inyecciones SQL y facilitar la reutilización.
- Ofrece control total sobre el resultado: puedes mapear manualmente filas (`Row`) a cualquier tipo de objeto (como
  `BookProjection`).
- Es ideal para escenarios donde `R2dbcEntityTemplate` se queda corto, como cuando se necesita trabajar con múltiples
  entidades o resultados no directamente relacionados con clases del modelo.

````java

@Slf4j
@RequiredArgsConstructor
@Repository
public class BookAuthorDaoImpl implements BookAuthorDao {

    private final DatabaseClient databaseClient;
    private static final String BOOK_ID = "bookId";
    private static final String AUTHOR_ID = "authorId";

    @Override
    public Mono<Long> countBookAuthorByCriteria(BookCriteria bookCriteria) {
        String sql = this.buildCountSql(bookCriteria);
        DatabaseClient.GenericExecuteSpec querySpec = this.databaseClient.sql(sql);
        querySpec = this.bindCriteriaParameters(querySpec, bookCriteria);
        return querySpec
                .map((row, rowMetadata) -> row.get("total", Long.class))
                .one();
    }

    @Override
    public Mono<Long> saveBookAuthor(BookAuthor bookAuthor) {
        return this.rowsUpdatedAfterInsert(bookAuthor);
    }

    @Override
    public Mono<Void> saveAllBookAuthor(List<BookAuthor> bookAuthorList) {
        return Flux.fromIterable(bookAuthorList)
                .flatMap(this::rowsUpdatedAfterInsert)
                .then();
    }

    @Override
    public Mono<Boolean> existBookAuthorByBookId(Integer bookId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM book_authors AS ba
                    WHERE ba.book_id = :bookId
                ) AS result
                """;
        return this.databaseClient
                .sql(sql)
                .bind(BOOK_ID, bookId)
                .map((row, rowMetadata) -> row.get("result", Boolean.class))
                .one();
    }

    @Override
    public Mono<Boolean> existBookAuthorByAuthorId(Integer authorId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM book_authors AS ba
                    WHERE ba.author_id = :authorId
                ) AS result
                """;
        return this.databaseClient
                .sql(sql)
                .bind(AUTHOR_ID, authorId)
                .map((row, rowMetadata) -> row.get("result", Boolean.class))
                .one();
    }

    @Override
    public Mono<Void> deleteBookAuthorByBookId(Integer bookId) {
        String sql = """
                DELETE FROM book_authors
                WHERE book_id = :bookId
                """;
        return this.databaseClient
                .sql(sql)
                .bind(BOOK_ID, bookId)
                .then();
    }

    @Override
    public Mono<Void> deleteBookAuthorByAuthorId(Integer authorId) {
        String sql = """
                DELETE FROM book_authors
                WHERE author_id = :authorId
                """;
        return this.databaseClient
                .sql(sql)
                .bind(AUTHOR_ID, authorId)
                .then();
    }

    @Override
    public Mono<BookProjection> findBookWithTheirAuthorsByBookId(Integer bookId) {
        String sql = """
                SELECT b.title,
                        b.publication_date,
                        b.online_availability,
                        STRING_AGG(a.first_name||' '||a.last_name, ', ') as concat_authors
                FROM books AS b
                    LEFT JOIN book_authors AS ba ON(b.id = ba.book_id)
                    LEFT JOIN authors AS a ON(ba.author_id = a.id)
                WHERE b.id = :bookId
                GROUP BY b.title,
                        b.publication_date,
                        b.online_availability
                """;
        return this.databaseClient
                .sql(sql)
                .bind(BOOK_ID, bookId)
                .map(this.mappingBookProjection())
                .one();
    }

    @Override
    public Flux<BookProjection> findAllToPage(BookCriteria bookCriteria, Pageable pageable) {
        String sql = this.buildDetailSql(bookCriteria);
        DatabaseClient.GenericExecuteSpec querySpec = this.databaseClient.sql(sql);
        querySpec = this.bindCriteriaParameters(querySpec, bookCriteria);
        querySpec = querySpec
                .bind("limit", pageable.getPageSize())
                .bind("offset", pageable.getOffset());
        return querySpec
                .map(this.mappingBookProjection())
                .all();
    }

    private BiFunction<Row, RowMetadata, BookProjection> mappingBookProjection() {
        return (row, rowMetadata) -> new BookProjection(
                row.get("title", String.class),
                row.get("publication_date", LocalDate.class),
                row.get("online_availability", Boolean.class),
                row.get("concat_authors", String.class)
        );
    }

    private Mono<Long> rowsUpdatedAfterInsert(BookAuthor bookAuthor) {
        String sql = """
                INSERT INTO book_authors(book_id, author_id)
                VALUES(:bookId, :authorId)
                """;
        return this.databaseClient
                .sql(sql)
                .bind(BOOK_ID, bookAuthor.getBookId())
                .bind(AUTHOR_ID, bookAuthor.getAuthorId())
                .fetch()
                .rowsUpdated();
    }

    private String buildDetailSql(BookCriteria bookCriteria) {
        StringBuilder sql = new StringBuilder("""
                SELECT b.title,
                        b.publication_date,
                        b.online_availability,
                        STRING_AGG(a.first_name||' '||a.last_name, ', ') as concat_authors
                FROM books AS b
                    LEFT JOIN book_authors AS ba ON(b.id = ba.book_id)
                    LEFT JOIN authors AS a ON(ba.author_id = a.id)
                """);
        sql.append(this.buildWhereClause(bookCriteria));
        sql.append("""
                GROUP BY b.title,
                        b.publication_date,
                        b.online_availability
                ORDER BY b.title
                LIMIT :limit
                OFFSET :offset
                """);
        return sql.toString();
    }

    private String buildCountSql(BookCriteria bookCriteria) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) AS total
                FROM (
                    SELECT b.id
                    FROM books AS b
                    LEFT JOIN book_authors AS ba ON (b.id = ba.book_id)
                    LEFT JOIN authors AS a ON (ba.author_id = a.id)
                """);
        sql.append(this.buildWhereClause(bookCriteria));
        sql.append("""
                    GROUP BY b.id
                ) AS unique_books
                """);
        return sql.toString();
    }

    private String buildWhereClause(BookCriteria bookCriteria) {
        List<String> conditions = new ArrayList<>();

        if (bookCriteria.hasQuery()) {
            conditions.add("""
                    (
                        LOWER(b.title) LIKE LOWER(:query)
                        OR LOWER(a.first_name) LIKE LOWER(:query)
                        OR LOWER(a.last_name) LIKE LOWER(:query)
                    )
                    """);
        }

        if (bookCriteria.hasPublicationDate()) {
            conditions.add("b.publication_date = :publicationDate");
        }

        if (conditions.isEmpty()) {
            return "";
        }

        return "WHERE %s%n".formatted(String.join(" AND ", conditions));
    }

    private DatabaseClient.GenericExecuteSpec bindCriteriaParameters(DatabaseClient.GenericExecuteSpec spec, BookCriteria bookCriteria) {
        if (bookCriteria.hasQuery()) {
            String likePattern = "%" + bookCriteria.query().trim() + "%";
            spec = spec.bind("query", likePattern);
        }

        if (bookCriteria.hasPublicationDate()) {
            spec = spec.bind("publicationDate", bookCriteria.publicationDate());
        }
        return spec;
    }
}
````

## ⚖️ Comparación: `R2dbcEntityTemplate` vs. `DatabaseClient`

| Característica               | `R2dbcEntityTemplate`                    | `DatabaseClient`                                 |
|------------------------------|------------------------------------------|--------------------------------------------------|
| 🔍 Nivel de abstracción      | Alto (orientado a entidades)             | Bajo (SQL nativo)                                |
| 🧱 Tipo de consulta          | Programática (Criteria API)              | SQL manual                                       |
| 🤝 Soporte para joins        | ❌ No                                     | ✅ Sí                                             |
| 🎯 Uso típico                | Consultas simples sobre una sola entidad | Consultas complejas, joins, vistas, proyecciones |
| 🧑‍💻 Control sobre SQL      | Limitado                                 | Total                                            |
| 🧩 Símil en stack bloqueante | `EntityManager` (JPA)                    | `JdbcTemplate`                                   |

### 📝 ¿Cuándo usar cada uno?

- Usa `R2dbcEntityTemplate` cuando necesitas construir consultas reactivas simples, con filtros dinámicos sobre una sola
  entidad, sin necesidad de escribir SQL directamente.
- Usa `DatabaseClient` cuando necesitas mayor flexibilidad, trabajar con múltiples tablas, funciones agregadas,
  paginación avanzada o SQL personalizado.

## 🔄 Mapper con MapStruct

Creamos la interfaz `BookMapper` para realizar la conversión (`mapping`) entre diferentes tipos de objetos
relacionados con la entidad `Book`. Utiliza `MapStruct`, un generador de código de mapeo que crea implementaciones
en tiempo de compilación, evitando así la necesidad de escribir conversiones manuales.

Este mapper en particular realiza:

- Conversión de entidad `Book` a `DTO` de respuesta (`BookResponse`).
- Conversión de `DTO` de entrada (`BookRequest`) a entidad `Book`.
- Actualización parcial de una entidad `Book` a partir de un `DTO` de actualización (`BookUpdateRequest`), ignorando el
  campo id.

### 🏷️ Anotaciones utilizadas

`@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)`

- Indica que esta interfaz es un mapper de `MapStruct`.
- `componentModel = "spring"` permite que la implementación generada sea un `bean` de `Spring`, de modo que pueda ser
  inyectada con `@Autowired` o `@RequiredArgsConstructor`.

`@Mapping(target = "id", ignore = true)`

- Se aplica al método `toBookUpdate`.
- Indica que el campo `id` de la entidad `Book` no debe ser sobrescrito durante el mapeo desde el `DTO`.
- Es útil cuando actualizamos una entidad existente, pero queremos conservar su identificador.

`@MappingTarget`

- Apunta al parámetro que se va a modificar directamente durante el mapeo.
- En este caso, permite que book sea actualizado en lugar de crear un nuevo objeto.
- Es ideal para operaciones `PUT` o `PATCH` donde actualizas una entidad persistente.

````java

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookMapper {
    BookResponse toBookResponse(Book book);

    Book toBook(BookRequest bookRequest);

    @Mapping(target = "id", ignore = true)
    Book toBookUpdate(@MappingTarget Book book, BookUpdateRequest bookUpdateRequest);
}
````

Para el mapeo de la entidad Author con su dto también definimos mapeador (`AuthorMapper`).

````java

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthorMapper {
    AuthorResponse toAuthorResponse(Author author);

    Author toAuthor(AuthorRequest authorRequest);
}
````

## ❌ Manejo centralizado de errores: `ApplicationExceptions`

`ApplicationExceptions` es una clase utilitaria diseñada para centralizar la construcción de errores reactivos
(`Mono.error`) dentro de la aplicación. Esto permite lanzar excepciones personalizadas de forma consistente y
reutilizable desde cualquier componente reactivo (servicios, DAOs, validaciones, etc.).

`@NoArgsConstructor(access = AccessLevel.PRIVATE)`

- Genera un constructor privado sin argumentos.
- Esto impide que se creen instancias de la clase, forzando su uso estático.
- Refuerza la intención de que esta clase sea solo utilitaria.

````java

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationExceptions {

    public static <T> Mono<T> authorNotFound(Integer authorId) {
        return Mono.error(() -> new AuthorNotFoundException(authorId));
    }

    public static <T> Mono<T> bookNotFound(Integer bookId) {
        return Mono.error(() -> new BookNotFoundException(bookId));
    }

    public static <T> Mono<T> authorIdsNotFound() {
        return Mono.error(AuthorIdsNotFoundException::new);
    }

    public static <T> Mono<T> missingFirstName() {
        return Mono.error(() -> new InvalidInputException("El nombre es requerido"));
    }

    public static <T> Mono<T> missingLastName() {
        return Mono.error(() -> new InvalidInputException("El apellido es requerido"));
    }

    public static <T> Mono<T> missingBirthdate() {
        return Mono.error(() -> new InvalidInputException("La fecha de nacimiento es requerido"));
    }
}
````

A continuación se muestran las distintas clases de excepción que utiliza la clase `ApplicationExceptions`.

````java
public class AuthorNotFoundException extends RuntimeException {

    private static final String MESSAGE = "El author [id=%d] no fue encontrado";

    public AuthorNotFoundException(Integer authorId) {
        super(MESSAGE.formatted(authorId));
    }
}
````

````java
public class BookNotFoundException extends RuntimeException {

    private static final String MESSAGE = "El libro [id=%d] no fue encontrado";

    public BookNotFoundException(Integer authorId) {
        super(MESSAGE.formatted(authorId));
    }
}
````

````java
public class AuthorIdsNotFoundException extends RuntimeException {
    public AuthorIdsNotFoundException() {
        super("Algunos IDs de autores no existen en el sistema");
    }
}
````

````java
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
````

## 🧱 Manejo global de excepciones HTTP: `ApplicationExceptionHandler`

`ApplicationExceptionHandler` es una clase anotada con `@RestControllerAdvice` que actúa como manejador global de
excepciones para toda la aplicación `WebFlux`.

Su responsabilidad principal es interceptar excepciones personalizadas o comunes, y transformarlas en respuestas HTTP
con estructura clara y uniforme, usando el tipo `ProblemDetail`, definido en `spring-web`.

### 🧩 ¿Qué hace?

- Captura excepciones lanzadas desde controladores y servicios (como `AuthorNotFoundException`, `InvalidInputException`,
  etc.).
- Crea una instancia de `ProblemDetail` que representa un error HTTP estructurado.
- Devuelve la excepción traducida como `ResponseEntity<ProblemDetail>` con el código de estado correspondiente (`404`,
  `400`, `500`, etc.).
- Añade detalles adicionales como:
    - `title`: resumen del error.
    - `detail`: mensaje técnico de la excepción.
    - Propiedades adicionales como `errors` en caso de validación.

````java

@Slf4j
@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(AuthorNotFoundException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(AuthorNotFoundException exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.NOT_FOUND, exception, detail -> {
            detail.setTitle("Autor no encontrado");
        });
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail));
    }

    @ExceptionHandler(InvalidInputException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(InvalidInputException exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.BAD_REQUEST, exception, detail -> {
            detail.setTitle("Entrada no válida");
        });
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleDecodingException(ServerWebInputException exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.BAD_REQUEST, exception, detail -> {
            detail.setTitle("Error de formato de la petición");
            log.info("{}", exception.getMostSpecificCause().getMessage());
        });
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail));
    }

    @ExceptionHandler(AuthorIdsNotFoundException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(AuthorIdsNotFoundException exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.NOT_FOUND, exception, detail -> {
            detail.setTitle("Autor no encontrado");
        });
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail));
    }

    @ExceptionHandler(BookNotFoundException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(BookNotFoundException exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.NOT_FOUND, exception, detail -> {
            detail.setTitle("Libro no encontrado");
        });
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(WebExchangeBindException exception) {
        Map<String, List<String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                                DefaultMessageSourceResolvable::getDefaultMessage,
                                Collectors.toList()
                        )
                ));
        ProblemDetail problemDetail = this.build(HttpStatus.BAD_REQUEST, exception, detail -> {
            detail.setTitle("El cuerpo de la petición contiene valores no válidos");
            detail.setDetail(exception.getReason());
            detail.setProperty("errors", errors);
        });
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(Exception exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.INTERNAL_SERVER_ERROR, exception, detail -> {
            detail.setTitle("Se produjo un error interno en el servidor");
        });
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail));
    }

    private ProblemDetail build(HttpStatus status, Exception exception, Consumer<ProblemDetail> detailConsumer) {
        log.info("{}", exception.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        detailConsumer.accept(problemDetail);
        return problemDetail;
    }
}
````

## 🧩 Servicio de dominio: `AuthorService`

La interfaz `AuthorService` define las operaciones principales del dominio `Author`, desacoplando la lógica de negocio
de su implementación. Proporciona soporte para:

- Listado total de autores.
- Búsqueda por ID con proyección.
- Paginación con filtros.
- Creación, actualización y eliminación de autores.

````java
public interface AuthorService {
    Flux<AuthorResponse> findAllAuthors();

    Mono<AuthorProjection> findAuthorById(Integer authorId);

    Mono<Page<AuthorProjection>> getAllAuthorsToPage(String query, int pageNumber, int pageSize);

    Mono<Integer> saveAuthor(AuthorRequest authorRequest);

    Mono<AuthorProjection> updateAuthor(Integer authorId, AuthorRequest authorRequest);

    Mono<Boolean> deleteAuthor(Integer authorId);
}
````

La clase `AuthorServiceImpl` contiene la lógica de negocio reactiva para trabajar con autores, apoyándose en
componentes como:

- `AuthorRepository`: acceso principal a la base de datos.
- `BookAuthorDao`: verificación y eliminación de relaciones entre libros y autores.
- `AuthorMapper`: conversión entre entidades y DTOs.

### 🔍 Comportamientos destacados

- `Lectura reactiva y eficiente`: métodos como `findAllAuthors()` o `getAllAuthorsToPage()` devuelven flujos
  (`Flux` / `Mono`) desde repositorios, manteniendo la naturaleza no bloqueante de `WebFlux`.
- `Paginación con conteo total`: se utiliza `Mono.zip(...)` para `obtener simultáneamente` la lista paginada y el total
  de resultados, y luego se construye un objeto `PageImpl`.
- `Validación de existencia`: se centraliza la validación de existencia con
  `switchIfEmpty(ApplicationExceptions.authorNotFound(...))`, asegurando respuestas consistentes.
- `Mapeo de DTOs`: se usa `AuthorMapper` para convertir entre `AuthorRequest` y `Author`, separando claramente las
  capas.
- `Eliminación con relación`: al eliminar un autor, se verifica si tiene libros relacionados. Si los tiene, se eliminan
  primero de la tabla intermedia (`book_authors`) antes de eliminar el autor.

````java

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final BookAuthorDao bookAuthorDao;
    private final AuthorMapper authorMapper;

    @Override
    @Transactional(readOnly = true)
    public Flux<AuthorResponse> findAllAuthors() {
        return this.authorRepository.findAll()
                .map(this.authorMapper::toAuthorResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AuthorProjection> findAuthorById(Integer authorId) {
        return this.authorRepository.findAuthorById(authorId)
                .switchIfEmpty(ApplicationExceptions.authorNotFound(authorId));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Page<AuthorProjection>> getAllAuthorsToPage(String query, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return Mono.zip(
                this.authorRepository.findByQuery(query, pageable).collectList(),
                this.authorRepository.findCountByQuery(query),
                (data, total) -> new PageImpl<>(data, pageable, total)
        );
    }

    @Override
    @Transactional
    public Mono<Integer> saveAuthor(AuthorRequest authorRequest) {
        return Mono.fromSupplier(() -> this.authorMapper.toAuthor(authorRequest))
                .flatMap(this.authorRepository::saveAuthor);
    }

    @Override
    @Transactional
    public Mono<AuthorProjection> updateAuthor(Integer authorId, AuthorRequest authorRequest) {
        return this.authorRepository.findById(authorId)
                .map(author -> this.authorMapper.toAuthor(authorRequest))
                .doOnNext(author -> author.setId(authorId))
                .flatMap(this.authorRepository::updateAuthor)
                .flatMap(affectedRows -> this.authorRepository.findAuthorById(authorId))
                .switchIfEmpty(ApplicationExceptions.authorNotFound(authorId));
    }

    @Override
    @Transactional
    public Mono<Boolean> deleteAuthor(Integer authorId) {
        return this.authorRepository.findById(authorId)
                .switchIfEmpty(ApplicationExceptions.authorNotFound(authorId))
                .flatMap(author -> this.bookAuthorDao.existBookAuthorByAuthorId(authorId))
                .flatMap(hasBooks -> Boolean.TRUE.equals(hasBooks) ? this.bookAuthorDao.deleteBookAuthorByAuthorId(authorId) : Mono.empty())
                .then(this.authorRepository.deleteAuthorById(authorId));
    }
}
````

## 📘 Servicio de dominio: `BookService`

La interfaz `BookService` define las operaciones del dominio libro (`Book`) y su relación con autores. Expone
funcionalidades esenciales como:

- Listado completo y búsqueda por ID.
- Paginación con criterios de búsqueda.
- Registro, actualización y eliminación de libros.
- Mantenimiento de la relación muchos a muchos entre libros y autores.

````java
public interface BookService {
    Flux<BookResponse> findAllBooks();

    Mono<BookProjection> findBookById(Integer bookId);

    Mono<Page<BookProjection>> findBooksWithAuthorsByCriteria(String query, LocalDate publicationDate, int pageNumber, int pageSize);

    Mono<BookProjection> saveBook(BookRequest bookRequest);

    Mono<BookProjection> updateBook(Integer bookId, BookUpdateRequest bookUpdateRequest);

    Mono<BookProjection> updateBookAuthors(Integer bookId, List<Integer> authorIds);

    Mono<Void> deleteBook(Integer bookId);
}
````

`BookServiceImpl` implementa la lógica de negocio relacionada con los libros, integrando varios componentes como:

- `BookRepository`: acceso principal a la tabla de libros.
- `AuthorRepository`: para verificar existencia de autores.
- `BookAuthorDao`: para manipular relaciones libro-autor.
- `BookMapper`: convierte entre entidades y DTOs.

### 🔍 Comportamientos clave

- `Relaciones libro-autor`: en métodos como `saveBook()` y `updateBookAuthors()`, se valida que los IDs de autores
  existan antes de registrar la relación en la tabla intermedia `(book_authors`).
- `Manejo de proyecciones`: se devuelve una proyección personalizada (`BookProjection`) con datos enriquecidos,
  incluyendo los autores concatenados.
- `Criterios dinámicos`: en la búsqueda paginada (`findBooksWithAuthorsByCriteria`), se aplican filtros opcionales como
  título o fecha de publicación usando `BookCriteria`.
- `Eliminación segura`: antes de eliminar un libro, se verifica y elimina su relación con los autores si corresponde.
- `Uso de operadores reactivos`: se utiliza `Mono.defer`, `Mono.fromSupplier`, `Mono.zip` y otros patrones para
  garantizar operaciones no bloqueantes y eficientes.

````java

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookAuthorDao bookAuthorDao;
    private final BookMapper bookMapper;

    @Override
    @Transactional(readOnly = true)
    public Flux<BookResponse> findAllBooks() {
        return this.bookRepository.findAll()
                .map(this.bookMapper::toBookResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BookProjection> findBookById(Integer bookId) {
        return this.bookAuthorDao.findBookWithTheirAuthorsByBookId(bookId)
                .switchIfEmpty(ApplicationExceptions.bookNotFound(bookId));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Page<BookProjection>> findBooksWithAuthorsByCriteria(String query, LocalDate publicationDate, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        BookCriteria bookCriteria = new BookCriteria(query, publicationDate);
        return Mono.zip(
                this.bookAuthorDao.findAllToPage(bookCriteria, pageable).collectList(),
                this.bookAuthorDao.countBookAuthorByCriteria(bookCriteria),
                (data, total) -> new PageImpl<>(data, pageable, total)
        );
    }

    @Override
    @Transactional
    public Mono<BookProjection> saveBook(BookRequest bookRequest) {
        return this.validateAuthors(bookRequest)
                .then(Mono.fromSupplier(() -> this.bookMapper.toBook(bookRequest)))
                .flatMap(this.bookRepository::save)
                .flatMap(savedBook -> {
                    if (bookRequest.hasNoAuthorIds()) {
                        return Mono.just(savedBook);
                    }
                    List<BookAuthor> relations = this.bookAuthorList(bookRequest.authorIds(), savedBook.getId());
                    return this.bookAuthorDao.saveAllBookAuthor(relations)
                            .thenReturn(savedBook);
                })
                .flatMap(savedBook -> this.bookAuthorDao.findBookWithTheirAuthorsByBookId(savedBook.getId()));
    }

    @Override
    @Transactional
    public Mono<BookProjection> updateBook(Integer bookId, BookUpdateRequest bookUpdateRequest) {
        return this.bookRepository.findById(bookId)
                .map(book -> this.bookMapper.toBookUpdate(book, bookUpdateRequest))
                .flatMap(this.bookRepository::save)
                .flatMap(savedBook -> this.bookAuthorDao.findBookWithTheirAuthorsByBookId(bookId))
                .switchIfEmpty(ApplicationExceptions.bookNotFound(bookId));
    }

    @Override
    @Transactional
    public Mono<BookProjection> updateBookAuthors(Integer bookId, List<Integer> authorIds) {
        return this.bookRepository.findById(bookId)
                .switchIfEmpty(ApplicationExceptions.bookNotFound(bookId))
                .flatMap(book -> {
                    if (Objects.isNull(authorIds) || authorIds.isEmpty()) {
                        return this.bookAuthorDao.deleteBookAuthorByBookId(bookId);
                    }
                    return this.validateAuthors(authorIds)
                            .then(this.bookAuthorDao.deleteBookAuthorByBookId(bookId))
                            .then(Mono.fromSupplier(() -> this.bookAuthorList(authorIds, bookId)))
                            .flatMap(this.bookAuthorDao::saveAllBookAuthor);
                })
                .then(this.bookAuthorDao.findBookWithTheirAuthorsByBookId(bookId));
    }

    @Override
    @Transactional
    public Mono<Void> deleteBook(Integer bookId) {
        return this.bookRepository.findById(bookId)
                .switchIfEmpty(ApplicationExceptions.bookNotFound(bookId))
                .flatMap(book -> this.bookAuthorDao.existBookAuthorByBookId(bookId))
                .flatMap(hasAuthors -> Boolean.TRUE.equals(hasAuthors) ? this.bookAuthorDao.deleteBookAuthorByBookId(bookId) : Mono.empty())
                .then(this.bookRepository.deleteById(bookId));
    }

    // Mono.fromSupplier(...): internamente debes retornar un valor simple (un objeto). El resultado es un Mono que emite ese valor al suscribirse.
    // Mono.defer(...): internamente debes retornar un Mono. El resultado es exactamente ese Mono retornado (no lo crea hasta que se suscriba).
    private Mono<Void> validateAuthors(BookRequest bookRequest) {
        return Mono.defer(() -> {
            if (bookRequest.hasNoAuthorIds()) {
                return Mono.empty();
            }
            return this.allAuthorIdsExist(bookRequest.authorIds());
        });
    }

    private Mono<Void> validateAuthors(List<Integer> authorIds) {
        return Mono.defer(() -> this.allAuthorIdsExist(authorIds));
    }

    private Mono<Void> allAuthorIdsExist(List<Integer> authorIds) {
        return this.authorRepository.findAllAuthorsByIdIn(authorIds)
                .collectList()
                .flatMap(authors -> {
                    if (authorIds.size() != authors.size()) {
                        return ApplicationExceptions.authorIdsNotFound();
                    }
                    return Mono.empty();
                });
    }

    private List<BookAuthor> bookAuthorList(List<Integer> authorIds, Integer bookId) {
        return authorIds.stream()
                .map(authorId -> BookAuthor.builder()
                        .bookId(bookId)
                        .authorId(authorId)
                        .build()
                )
                .toList();
    }
}
````

## 📄 Anotación `@Transactional` en aplicaciones reactivas con Spring WebFlux y R2DBC

### 🎯 Objetivo General

Verificar y documentar el uso de la anotación `@Transactional` en aplicaciones reactivas, específicamente en un
proyecto con:

- Spring WebFlux
- Spring Data R2DBC
- PostgreSQL

Se sabe de antemano que:

> ✅ La anotación `@Transactional` funciona correctamente en aplicaciones reactivas para delimitar transacciones que
> modifican la base de datos (`INSERT`, `UPDATE`, `DELETE`).

⚠️ El enfoque de estas pruebas fue comprobar específicamente que `@Transactional(readOnly = true)` efectivamente
bloquea escrituras.

Para ello se diseñaron experimentos que forzaran un `INSERT` bajo una transacción marcada como de `solo lectura`.

### ⚙️ Entorno de Pruebas

- Spring Boot: 3.5.3
- Spring Data R2DBC: versión incluida en Spring Boot 3.5.3
- Driver R2DBC: io.r2dbc:r2dbc-postgresql
- Base de datos: PostgreSQL
- Configuración adicional:
    - Únicamente propiedades de conexión en application.yml
    - Logs SQL habilitados
    - Sin TransactionManager personalizado

### 🧪 Pruebas realizadas

A continuación se detallan las pruebas y resultados.

### 🟢 1️⃣ Prueba `SIN` `@Transactional(readOnly = true)`

📌 Código del método

````java

@Override
public Flux<AuthorResponse> findAllAuthors() {
    return this.authorRepository.save(Author.builder()
                    .firstName("Ale")
                    .lastName("Flo")
                    .birthdate(LocalDate.parse("2025-05-06"))
                    .build())
            .doOnNext(author -> log.info("{}", author))
            .flatMapMany(author -> this.authorRepository.findAll())
            .map(this.authorMapper::toAuthorResponse);
}
````

📋 Resultado observado

- Se ejecutó correctamente un INSERT.
- Luego se ejecutó el SELECT de todos los autores.
- La respuesta incluyó el autor recién insertado.
- No se produjo ningún error.

Log en el Ide

````bash
io.r2dbc.postgresql.PARAM                : Bind parameter [0] to: Ale
io.r2dbc.postgresql.PARAM                : Bind parameter [1] to: Flo
io.r2dbc.postgresql.PARAM                : Bind parameter [2] to: 2025-05-06
io.r2dbc.postgresql.QUERY                : Executing query: INSERT INTO authors (first_name, last_name, birthdate) VALUES ($1, $2, $3) RETURNING id
d.m.r.a.service.impl.AuthorServiceImpl   : Author(id=5, firstName=Ale, lastName=Flo, birthdate=2025-05-06)
io.r2dbc.postgresql.QUERY                : Executing query: SELECT authors.* FROM authors
````

Log en el cliente

````bash
$ curl -v http://localhost:8080/api/v1/authors/stream
>
< HTTP/1.1 200 OK
< transfer-encoding: chunked
< Content-Type: text/event-stream;charset=UTF-8
<
data:{"id":1,"firstName":"Milagros","lastName":"Díaz","birthdate":"2006-06-15"}

data:{"id":2,"firstName":"Lesly","lastName":"Águila","birthdate":"1995-06-09"}

data:{"id":3,"firstName":"Kiara","lastName":"Lozano","birthdate":"2001-10-03"}

data:{"id":4,"firstName":"Briela","lastName":"Cirilo","birthdate":"1997-09-25"}

data:{"id":5,"firstName":"Ale","lastName":"Flo","birthdate":"2025-05-06"}
````

✅ Conclusión:
> El método `sin readOnly` permite insertar datos normalmente.

### 🔵 2️⃣ Prueba `CON` `@Transactional(readOnly = true)`

📌 Código del método

````java

@Override
@Transactional(readOnly = true)
public Flux<AuthorResponse> findAllAuthors() {
    return this.authorRepository.save(Author.builder()
                    .firstName("Ale")
                    .lastName("Flo")
                    .birthdate(LocalDate.parse("2025-05-06"))
                    .build())
            .doOnNext(author -> log.info("{}", author))
            .flatMapMany(author -> this.authorRepository.findAll())
            .map(this.authorMapper::toAuthorResponse);
}
````

📋 Resultado observado

- La transacción inició en modo `READ ONLY`.
- PostgreSQL `bloqueó la escritura`.
- Se produjo un `rollback` automático.
- La llamada devolvió `error HTTP 500`.

Log en el ide

````bash
DEBUG 17820 --- io.r2dbc.postgresql.QUERY                : Executing query: BEGIN READ ONLY
DEBUG 17820 --- io.r2dbc.postgresql.PARAM                : Bind parameter [0] to: Ale
DEBUG 17820 --- io.r2dbc.postgresql.PARAM                : Bind parameter [1] to: Flo
DEBUG 17820 --- io.r2dbc.postgresql.PARAM                : Bind parameter [2] to: 2025-05-06
DEBUG 17820 --- io.r2dbc.postgresql.QUERY                : Executing query: INSERT INTO authors (first_name, last_name, birthdate) VALUES ($1, $2, $3) RETURNING id
DEBUG 17820 --- io.r2dbc.postgresql.QUERY                : Executing query: ROLLBACK
ERROR 17820 --- a.w.r.e.AbstractErrorWebExceptionHandler : [35475d0d-4]  500 Server Error for HTTP GET "/api/v1/authors/stream"

org.springframework.dao.DataAccessResourceFailureException: executeMany; SQL [INSERT INTO authors (first_name, last_name, birthdate) VALUES ($1, $2, $3)]; no se puede ejecutar INSERT en una transacción de sólo lectura
````

Log en el cliente

````bash
$ curl -v http://localhost:8080/api/v1/authors/stream | jq
>
< HTTP/1.1 500 Internal Server Error
< Content-Type: application/json
< Content-Length: 319
<
{
  "timestamp": "2025-06-29T00:35:33.603+00:00",
  "path": "/api/v1/authors/stream",
  "status": 500,
  "error": "Internal Server Error",
  "requestId": "35475d0d-4",
  "message": "executeMany; SQL [INSERT INTO authors (first_name, last_name, birthdate) VALUES ($1, $2, $3)]; no se puede ejecutar INSERT en una transacción de sólo lectura"
}
````

✅ Conclusión:
> `@Transactional(readOnly = true)` se traduce correctamente en `BEGIN READ ONLY` y
> `PostgreSQL bloquea cualquier INSERT`.

### ✨ Conclusiones finales

1. La anotación `@Transactional` en aplicaciones reactivas con `Spring WebFlux` y `R2DBC` funciona correctamente para
   delimitar transacciones que modifican la base de datos (`INSERT`, `UPDATE`, `DELETE`).
2. La anotación `@Transactional(readOnly = true)` también funciona correctamente, al iniciar la transacción en modo
   `READ ONLY` en `PostgreSQL`.
3. Al intentar insertar datos dentro de una transacción de solo lectura, `PostgreSQL` bloquea la operación con un error
   claro, y `Spring` realiza un `rollback automático`.
4. Estas pruebas demuestran de forma empírica que el soporte de `readOnly=true` está operativo en `Spring Boot 3.5.x`
   sin necesidad de configuraciones adicionales.

🔔 Importante

- `R2DBC + PostgreSQL`: la restricción de solo lectura la hace la base de datos.
- `JPA + Hibernate`: la restricción de solo lectura la hace Hibernate en la sesión.
- No todas las bases de datos se comportan igual.
- Siempre es buena práctica declarar la intención con `readOnly = true`.

## ✅ Validación manual de peticiones: `RequestValidator`

La clase `RequestValidator` proporciona una forma manual y programática de validar instancias de `AuthorRequest` en el
flujo reactivo. Está diseñada para ser usada directamente dentro de controladores o servicios, sin necesidad de
anotaciones como `@Valid` o `@NotNull`.

Este enfoque es útil cuando se desea tener un control más explícito sobre el proceso de validación, especialmente
cuando se desea encadenar validaciones dentro de un `Mono<T>` o `Flux<T>` antes de ejecutar una lógica de negocio.

### 🧪 ¿Cómo funciona?

- El método `validate()` devuelve una función (`UnaryOperator<Mono<AuthorRequest>>`) que puede aplicarse a un
  `Mono<AuthorRequest>` para validar sus campos.

- Internamente, esta función:
    - Verifica que firstName no sea null ni esté vacío.
    - Verifica que lastName no sea null ni esté vacío.
    - Verifica que birthdate no sea null.

- Si alguna validación falla, se retorna un `Mono.error(...)` con una excepción personalizada de
  `ApplicationExceptions`.

````java

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestValidator {

    public static UnaryOperator<Mono<AuthorRequest>> validate() {
        return authorRequestMono -> authorRequestMono
                .filter(hasFirstName())
                .switchIfEmpty(ApplicationExceptions.missingFirstName())
                .filter(hasLastName())
                .switchIfEmpty(ApplicationExceptions.missingLastName())
                .filter(hasBirthdate())
                .switchIfEmpty(ApplicationExceptions.missingBirthdate());
    }

    private static Predicate<AuthorRequest> hasFirstName() {
        return authorRequest -> Objects.nonNull(authorRequest.firstName())
                                && !authorRequest.firstName().trim().isEmpty();
    }

    private static Predicate<AuthorRequest> hasLastName() {
        return authorRequest -> Objects.nonNull(authorRequest.lastName())
                                && !authorRequest.lastName().trim().isEmpty();
    }

    private static Predicate<AuthorRequest> hasBirthdate() {
        return authorRequest -> Objects.nonNull(authorRequest.birthdate());
    }
}
````

## 🎯 Controlador para Autores: `AuthorController`

El controlador `AuthorController` expone los endpoints REST para gestionar autores. Está basado en `Spring WebFlux` y
ofrece soporte completo para operaciones reactivas como consulta, creación, actualización y eliminación de autores.

Una característica distintiva de este controlador es que implementa validación manual de entradas usando la clase
`RequestValidator`, en lugar de utilizar anotaciones como `@Valid`. Esta estrategia permite tener mayor control sobre
la lógica de validación y personalizar el flujo de errores en tiempo de ejecución.

### ✋ Validación manual con RequestValidator

Para los métodos `saveAuthor` y `updateAuthor`, el controlador utiliza `.transform(RequestValidator.validate())`. Esto
permite aplicar un conjunto de validaciones explícitas antes de continuar con la lógica de negocio. Si alguna
validación falla, se lanza una excepción controlada que será manejada posteriormente por el
`ApplicationExceptionHandler`.

### 📚 Endpoints expuestos

| Método / Ruta        | Descripción                                                             | 
|----------------------|-------------------------------------------------------------------------|
| `GET /stream`        | Devuelve un `Flux<AuthorResponse>` como flujo SSE (`text/event-stream`) |
| `GET /{authorId}`    | Busca un autor por su ID                                                |
| `GET /paginated`     | Devuelve una página de autores filtrados por nombre y apellido          |
| `POST /`             | Crea un nuevo autor luego de validar manualmente los datos recibidos    |
| `PUT /{authorId}`    | Actualiza un autor existente luego de aplicar validación                |
| `DELETE /{authorId}` | Elimina un autor si no tiene relación con libros                        |

````java

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/authors")
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<AuthorResponse>>> getAuthors() {
        return Mono.fromSupplier(() -> ResponseEntity.ok(this.authorService.findAllAuthors()));
    }

    @GetMapping(path = "/{authorId}")
    public Mono<ResponseEntity<AuthorProjection>> getAuthor(@PathVariable Integer authorId) {
        return this.authorService.findAuthorById(authorId)
                .map(ResponseEntity::ok);
    }

    @GetMapping(path = "/paginated")
    public Mono<ResponseEntity<Page<AuthorProjection>>> getPaginatedAuthors(@RequestParam(required = false, defaultValue = "") String query,
                                                                            @RequestParam(required = false, defaultValue = "0") int pageNumber,
                                                                            @RequestParam(required = false, defaultValue = "5") int pageSize) {
        return this.authorService.getAllAuthorsToPage(query, pageNumber, pageSize)
                .map(ResponseEntity::ok);

    }

    @PostMapping
    public Mono<ResponseEntity<Void>> saveAuthor(@RequestBody Mono<AuthorRequest> authorRequestMono) {
        return authorRequestMono
                .transform(RequestValidator.validate())
                .flatMap(this.authorService::saveAuthor)
                .map(affectedRows -> ResponseEntity.status(HttpStatus.CREATED).build());
    }

    @PutMapping(path = "/{authorId}")
    public Mono<ResponseEntity<AuthorProjection>> updateAuthor(@PathVariable Integer authorId,
                                                               @RequestBody Mono<AuthorRequest> authorRequestMono) {
        return authorRequestMono
                .transform(RequestValidator.validate())
                .flatMap(authorRequest -> this.authorService.updateAuthor(authorId, authorRequest))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(path = "/{authorId}")
    public Mono<ResponseEntity<Void>> deleteAuthor(@PathVariable Integer authorId) {
        return this.authorService.deleteAuthor(authorId)
                .map(wasDeleted -> ResponseEntity.noContent().build());
    }
}
````

## ✅ Validación declarativa con anotaciones: `BookController`

En el `BookController` se aplica la `validación automática` de peticiones usando la anotación `@Valid` junto con
`Spring Validation`. Este enfoque permite que las clases `BookRequest`, `BookUpdateRequest` y `BookAuthorUpdateRequest`
sean validadas automáticamente al recibir una solicitud HTTP. Si alguna restricción (como `@NotBlank`, `@Size`,
`@NotNull`, etc.) no se cumple, se lanza una excepción (`WebExchangeBindException`), la cual es gestionada globalmente
por el `ApplicationExceptionHandler`.

Gracias a este mecanismo, la lógica del controlador permanece limpia, delegando la validación al motor de
`Bean Validation` y centralizando el manejo de errores de entrada de forma elegante.

Este patrón es útil para validaciones estructurales o de formato que pueden definirse con anotaciones, y es
complementario al enfoque manual que usaste en el controlador de Author.

````java

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/books")
public class BookController {

    private final BookService bookService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<BookResponse>>> getBooks() {
        return Mono.fromSupplier(() -> ResponseEntity.ok(this.bookService.findAllBooks()));
    }

    @GetMapping(path = "/{bookId}")
    public Mono<ResponseEntity<BookProjection>> getBook(@PathVariable Integer bookId) {
        return this.bookService.findBookById(bookId)
                .map(ResponseEntity::ok);
    }

    @GetMapping(path = "/paginated")
    public Mono<ResponseEntity<Page<BookProjection>>> getPaginatedBooks(@RequestParam(required = false) LocalDate publicationDate,
                                                                        @RequestParam(required = false, defaultValue = "") String query,
                                                                        @RequestParam(required = false, defaultValue = "0") int pageNumber,
                                                                        @RequestParam(required = false, defaultValue = "5") int pageSize) {
        return this.bookService.findBooksWithAuthorsByCriteria(query, publicationDate, pageNumber, pageSize)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<BookProjection>> saveBook(@Valid @RequestBody Mono<BookRequest> bookRequestMono) {
        return bookRequestMono
                .flatMap(this.bookService::saveBook)
                .map(bookProjection -> ResponseEntity.status(HttpStatus.CREATED).body(bookProjection));
    }

    @PutMapping(path = "/{bookId}")
    public Mono<ResponseEntity<BookProjection>> updateBook(@PathVariable Integer bookId,
                                                           @Valid @RequestBody Mono<BookUpdateRequest> bookUpdateRequestMono) {
        return bookUpdateRequestMono
                .flatMap(bookUpdateRequest -> this.bookService.updateBook(bookId, bookUpdateRequest))
                .map(ResponseEntity::ok);
    }

    @PatchMapping(path = "/{bookId}/authors")
    public Mono<ResponseEntity<BookProjection>> updateBookAuthors(@PathVariable Integer bookId,
                                                                  @Valid @RequestBody Mono<BookAuthorUpdateRequest> authorUpdateRequestMono) {
        return authorUpdateRequestMono
                .flatMap(authorUpdateRequest -> this.bookService.updateBookAuthors(bookId, authorUpdateRequest.authorIds()))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(path = "/{bookId}")
    public Mono<ResponseEntity<Void>> deleteBook(@PathVariable Integer bookId) {
        return this.bookService.deleteBook(bookId)
                .thenReturn(ResponseEntity.noContent().build());

    }
}
````

---

# Pruebas de Integración

---

## Definiendo datos iniciales para las pruebas de integración

Vamos a crear dos archivos en nuestro classpath de `/test` que contendrán las instrucciones sql que ejecutaremos en cada
método de test de nuestro repositorio.

`src/test/resources/sql/data.sql`

````sql
INSERT INTO authors(first_name, last_name, birthdate)
VALUES
('Belén', 'Velez', '2006-06-15'),
('Marco', 'Salvador', '1995-06-09'),
('Greys', 'Briones', '2001-10-03'),
('Luis', 'Sánchez', '1997-09-25');

INSERT INTO books(title, publication_date, online_availability)
VALUES
('Los ríos profundos', '1999-01-15', true),
('La ciudad y los perros', '1985-03-18', true),
('El zorro de arriba y el zorro de abajo', '2002-05-06', false),
('Redoble por Rancas', '1988-07-15', true);

-- Book 1: tiene como author a Belén y Marco
INSERT INTO book_authors(book_id, author_id)
VALUES
(1, 1),
(1, 2);

-- Book 2: tiene como author a Greys
INSERT INTO book_authors(book_id, author_id)
VALUES
(2, 3);
````

`src/test/resources/sql/reset_test_data.sql`

````sql
TRUNCATE TABLE book_authors RESTART IDENTITY CASCADE;
TRUNCATE TABLE books RESTART IDENTITY CASCADE;
TRUNCATE TABLE authors RESTART IDENTITY CASCADE;
````

## 🧪 Clase base para pruebas de integración: `AbstractTest`

La clase `AbstractTest` sirve como base común para todas las pruebas de integración de la aplicación. Su propósito
principal es garantizar que cada prueba se ejecute sobre un estado consistente y controlado de la base de datos,
gracias a la carga previa de scripts SQL.

✅ Responsabilidades clave:

- `Ejecutar SQL de limpieza` (`reset_test_data.sql`): borra o reinicia el estado de la base de datos antes de cada
  prueba.
- `Ejecutar SQL de datos` (`data.sql`): inserta los datos necesarios para que las pruebas se ejecuten correctamente.
- Usa `DatabaseClient` para ejecutar estos scripts de forma reactiva, pero forzando la ejecución con `.block()` ya que
  se trata de código de preparación fuera del flujo reactivo de la prueba.

### 📌 Beneficio principal

Garantiza aislamiento entre pruebas, evita efectos colaterales y promueve repetibilidad, lo cual es esencial en pruebas
de integración reales que interactúan con una base de datos `PostgreSQL`.

````java
public abstract class AbstractTest {

    @Autowired
    private DatabaseClient databaseClient;
    private static String dataSQL;
    private static String resetTestData;

    @BeforeAll
    static void beforeAll() throws IOException {
        resetTestData = getSQL(new ClassPathResource("sql/reset_test_data.sql"));
        dataSQL = getSQL(new ClassPathResource("sql/data.sql"));
    }

    @BeforeEach
    void setUp() {
        this.executeSQL(resetTestData);
        this.executeSQL(dataSQL);
    }

    private static String getSQL(ClassPathResource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void executeSQL(String sql) {
        this.databaseClient.sql(sql)
                .fetch()
                .rowsUpdated()
                .block(); //Aunque se usa block(), está justificado aquí porque estás en una clase de prueba que no necesita mantener la naturaleza reactiva pura.
    }
}
````

**Nota**
> En esta documentación solo se incluyen las pruebas relacionadas con `Book` y `BookAuthor`.
> Las pruebas correspondientes a `Author` no se muestran aquí, ya que siguen una lógica similar.
> Si se desea revisarlas, se pueden consultar directamente en el código fuente del proyecto.

## 🧪 AuthorRepositoryTest

Esta clase contiene pruebas de integración para el componente `AuthorRepository`, donde se verifica que los métodos
personalizados funcionen correctamente al interactuar con una base de datos PostgreSQL real. Cada prueba asegura que
las operaciones como buscar, guardar, actualizar y paginar autores se comporten según lo esperado.

> Dado que todas las pruebas se ejecutan contra una base de datos real (usando datos cargados previamente), hablamos de
> `pruebas de integración` completas.

### ✅ Anotaciones explicadas

| Anotación              | Descripción                                                                                                                                                                                                                                                                                                        |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@Slf4j`               | Anotación de Lombok que genera automáticamente un logger (`log`) para la clase, permitiéndote usar `log.info(...)` sin necesidad de declararlo.                                                                                                                                                                    |
| `@DataR2dbcTest`       | Anotación de Spring Boot especializada para pruebas con R2DBC. <br>✅ **Carga sólo los beans relacionados a la capa de persistencia** (repositorios, configuración de R2DBC, etc.). <br>❌ **No carga beans de servicios o controladores**, lo cual acelera las pruebas. <br>Ideal para probar `Repository` o `DAO`. |
| `@Autowired`           | Inyecta automáticamente el `AuthorRepository` configurado por el contexto de prueba cargado por Spring Boot.                                                                                                                                                                                                       |
| `@Test`                | Marca un método como una prueba unitaria o de integración. Será ejecutado por el motor de pruebas (JUnit en este caso).                                                                                                                                                                                            |
| `extends AbstractTest` | Heredas el comportamiento de reinicializar la base de datos antes de cada prueba, asegurando un entorno **consistente y limpio** cada vez.                                                                                                                                                                         |

La anotación `@DataR2dbcTest`:

- Habilita solo los componentes de persistencia reactiva necesarios para probar con `R2DBC`.
- Configura una base de datos embebida o el `datasource configurado`.
- Excluye componentes Web, Beans externos, Controllers, etc.

> ✅ Ideal para pruebas de repositorios (`ReactiveCrudRepository`, `R2dbcEntityTemplate`, etc.) de forma rápida y
> aislada.

Antes de que se ejecuten todos los tests, se cargan los scripts `reset_test_data.sql` y `data.sql` desde el classpath.
Luego, antes de cada método de test, se ejecutan estos scripts para garantizar un entorno limpio y reproducible. Esto
asegura que los tests no dependan del orden de ejecución o del estado de datos compartido.

````java

@Slf4j
@DataR2dbcTest
class AuthorRepositoryTest extends AbstractTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void findAllAuthors() {
        this.authorRepository.findAllAuthorsByIdIn(List.of(2, 3))
                .doOnNext(author -> log.info("{}", author))
                .as(StepVerifier::create)
                .assertNext(author -> {
                    Assertions.assertEquals(2, author.getId());
                    Assertions.assertEquals("Marco", author.getFirstName());
                    Assertions.assertEquals("Salvador", author.getLastName());
                    Assertions.assertEquals(LocalDate.parse("1995-06-09"), author.getBirthdate());
                })
                .assertNext(author -> {
                    Assertions.assertEquals(3, author.getId());
                    Assertions.assertEquals("Greys", author.getFirstName());
                    Assertions.assertEquals("Briones", author.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2001-10-03"), author.getBirthdate());
                })
                .verifyComplete();
    }

    @Test
    void findAuthorById() {
        this.authorRepository.findAuthorById(1)
                .doOnNext(authorProjection -> log.info("{}", authorProjection))
                .as(StepVerifier::create)
                .assertNext(authorProjection -> {
                    Assertions.assertEquals("Belén", authorProjection.getFirstName());
                    Assertions.assertEquals("Velez", authorProjection.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2006-06-15"), authorProjection.getBirthdate());

                    // Comprobamos que el método por defecto de la proyección está funcionando
                    Assertions.assertEquals("Belén Velez", authorProjection.getFullName());
                })
                .verifyComplete();
    }

    @Test
    void findCountByQuery() {
        this.authorRepository.findCountByQuery("e")
                .as(StepVerifier::create)
                .assertNext(count -> Assertions.assertEquals(3, count))
                .verifyComplete();
    }

    @Test
    void findByQueryAndPageable() {
        this.authorRepository.findByQuery("e", PageRequest.of(0, 2))
                .doOnNext(authorProjection -> log.info("{}", authorProjection))
                .as(StepVerifier::create)
                .assertNext(authorProjection -> {
                    Assertions.assertEquals("Belén", authorProjection.getFirstName());
                    Assertions.assertEquals("Velez", authorProjection.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2006-06-15"), authorProjection.getBirthdate());
                    //default method
                    Assertions.assertEquals("Belén Velez", authorProjection.getFullName());
                })
                .assertNext(authorProjection -> {
                    Assertions.assertEquals("Greys", authorProjection.getFirstName());
                    Assertions.assertEquals("Briones", authorProjection.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2001-10-03"), authorProjection.getBirthdate());
                    //default method
                    Assertions.assertEquals("Greys Briones", authorProjection.getFullName());
                })
                .verifyComplete();
    }

    @Test
    void saveAuthor() {
        Author author = Author.builder()
                .firstName("Susana")
                .lastName("Alvarado")
                .birthdate(LocalDate.parse("2000-11-07"))
                .build();
        this.authorRepository.saveAuthor(author)
                .doOnNext(affectedRows -> log.info("affectedRows: {}", affectedRows))
                .as(StepVerifier::create)
                .assertNext(affectedRows -> Assertions.assertEquals(1, affectedRows))
                .verifyComplete();

        this.authorRepository.count()
                .as(StepVerifier::create)
                .expectNext(5L)
                .verifyComplete();

        this.authorRepository.findById(5)
                .doOnNext(authorRetrieved -> log.info("{}", authorRetrieved))
                .as(StepVerifier::create)
                .assertNext(authorRetrieved -> {
                    Assertions.assertNotNull(authorRetrieved.getId());
                    Assertions.assertEquals("Susana", authorRetrieved.getFirstName());
                    Assertions.assertEquals("Alvarado", authorRetrieved.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2000-11-07"), authorRetrieved.getBirthdate());
                })
                .verifyComplete();

        this.authorRepository.deleteById(5)
                .then(this.authorRepository.count())
                .as(StepVerifier::create)
                .expectNext(4L)
                .verifyComplete();
    }

    @Test
    void updateCustomer() {
        this.authorRepository.findById(1)
                .doOnNext(author -> log.info("{}", author))
                .doOnNext(author -> author.setFirstName("Belencita"))
                .flatMap(author -> this.authorRepository.updateAuthor(author))
                .as(StepVerifier::create)
                .assertNext(affectedRows -> Assertions.assertEquals(1, affectedRows))
                .verifyComplete();

        this.authorRepository.findById(1)
                .doOnNext(author -> log.info("{}", author))
                .as(StepVerifier::create)
                .assertNext(author -> Assertions.assertEquals("Belencita", author.getFirstName()))
                .verifyComplete();
    }
}
````

## 🧪 `BookAuthorDaoImplTest`

Esta clase valida el correcto funcionamiento de todos los métodos definidos en la clase `BookAuthorDaoImpl`. Se enfoca
en
asegurar que las consultas SQL, inserciones, actualizaciones, eliminaciones y búsquedas por criterios personalizados
(como `BookCriteria`) se comporten como se espera al interactuar con una base de datos PostgreSQL real.

Estas pruebas son de integración porque:

- Se ejecutan contra una base de datos real (PostgreSQL, inicializada con scripts SQL).
- Evalúan cómo se comporta el DAO en conjunto con la configuración de R2DBC y la base de datos.
- Validan el resultado final de una operación completa, no solo el comportamiento de una función aislada.

### ✅ Anotaciones explicadas

| Anotación                          | ¿Para qué sirve?                                                                                                                                                                                                                                                                                                                                |
|------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@Slf4j`                           | Anotación de **Lombok** que autogenera el objeto `log`, útil para imprimir información durante las pruebas (debug/log).                                                                                                                                                                                                                         |
| `@DataR2dbcTest`                   | Anotación de **Spring Boot** que configura un contexto de pruebas limitado **solo a la capa de persistencia reactiva**. <br>Incluye: beans de configuración R2DBC, beans de `DatabaseClient`, repositorios reactivos, etc. <br>Excluye: servicios, controladores, seguridad, etc. Esto hace que el arranque del contexto sea rápido y enfocado. |
| `@Import(BookAuthorDaoImpl.class)` | Importa manualmente el bean de `BookAuthorDaoImpl`, ya que este no es un `@Repository` estándar ni se detecta automáticamente con `@DataR2dbcTest`. <br>**Sin esta anotación**, Spring no sabría cómo inyectar `BookAuthorDao`.                                                                                                                 |
| `@Autowired`                       | Inyecta el DAO real (`BookAuthorDaoImpl`) para usarlo dentro de los tests.                                                                                                                                                                                                                                                                      |
| `extends AbstractTest`             | Hereda de una clase que **reinicializa los datos de prueba** (`data.sql`, `reset_test_data.sql`) antes de cada test, garantizando que todos los tests comienzan desde un estado limpio.                                                                                                                                                         |

````java

@Slf4j
@Import(BookAuthorDaoImpl.class)
@DataR2dbcTest
class BookAuthorDaoImplTest extends AbstractTest {

    @Autowired
    private BookAuthorDao bookAuthorDao;

    @Test
    void shouldReturnTotalCountFilteredByQuery() {
        var criteria = new BookCriteria("ri", null);
        this.bookAuthorDao.countBookAuthorByCriteria(criteria)
                .as(StepVerifier::create)
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void shouldReturnTotalCountFilteredByPublicationDate() {
        var criteria = new BookCriteria("", LocalDate.parse("1988-07-15"));
        this.bookAuthorDao.countBookAuthorByCriteria(criteria)
                .as(StepVerifier::create)
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void shouldReturnTotalCountFilteredByQueryAndPublicationDate() {
        var criteria = new BookCriteria("ciu", LocalDate.parse("1985-03-18"));
        this.bookAuthorDao.countBookAuthorByCriteria(criteria)
                .as(StepVerifier::create)
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void shouldReturnTotalCountWithoutFilter() {
        var criteria = new BookCriteria(" ", null);
        this.bookAuthorDao.countBookAuthorByCriteria(criteria)
                .as(StepVerifier::create)
                .expectNext(4L)
                .verifyComplete();
    }

    @Test
    void saveBookAuthor() {
        this.bookAuthorDao.saveBookAuthor(new BookAuthor(4, 3))
                .as(StepVerifier::create)
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void saveBookAuthorExpectError() {
        this.bookAuthorDao.saveBookAuthor(new BookAuthor(1, 1))
                .as(StepVerifier::create)
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void saveAllBookAuthor() {
        this.bookAuthorDao.saveAllBookAuthor(List.of(
                        new BookAuthor(3, 1),
                        new BookAuthor(4, 1),
                        new BookAuthor(2, 2)
                ))
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void saveAllBookAuthorExpectError() {
        this.bookAuthorDao.saveAllBookAuthor(List.of(
                        new BookAuthor(3, 1),
                        new BookAuthor(4, 1),
                        new BookAuthor(1, 1)
                ))
                .as(StepVerifier::create)
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void existBookAuthorByBookId() {
        this.bookAuthorDao.existBookAuthorByBookId(1)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void notExistBookAuthorByBookId() {
        this.bookAuthorDao.existBookAuthorByBookId(3)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void existBookAuthorByAuthorId() {
        this.bookAuthorDao.existBookAuthorByAuthorId(1)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void notExistBookAuthorByAuthorId() {
        this.bookAuthorDao.existBookAuthorByAuthorId(4)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deleteBookAuthorByBookId() {
        // Verificamos que exista por el BookId
        this.bookAuthorDao.existBookAuthorByBookId(1)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // Eliminamos
        this.bookAuthorDao.deleteBookAuthorByBookId(1)
                .as(StepVerifier::create)
                .verifyComplete();

        // Verificamos que ya no existe
        this.bookAuthorDao.existBookAuthorByBookId(1)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deleteBookAuthorByBookIdWhenBookIdNotExist() {
        // Verificamos que no existe
        this.bookAuthorDao.existBookAuthorByBookId(3)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();

        // Eliminamos
        this.bookAuthorDao.deleteBookAuthorByBookId(3)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void deleteBookAuthorByAuthorId() {
        // Verificamos que exista por el AuthorId
        this.bookAuthorDao.existBookAuthorByAuthorId(1)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // Eliminamos
        this.bookAuthorDao.deleteBookAuthorByAuthorId(1)
                .as(StepVerifier::create)
                .verifyComplete();

        // Verificamos que ya no existe
        this.bookAuthorDao.existBookAuthorByAuthorId(1)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deleteBookAuthorByBookIdWhenAuthorIdNotExist() {
        // Verificamos que no existe
        this.bookAuthorDao.existBookAuthorByAuthorId(4)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();

        // Eliminamos
        this.bookAuthorDao.deleteBookAuthorByAuthorId(4)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findBookWithTheirAuthorsByBookId() {
        this.bookAuthorDao.findBookWithTheirAuthorsByBookId(1)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("Los ríos profundos", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1999-01-15"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertEquals("Belén Velez, Marco Salvador", bookProjection.authors());
                    Assertions.assertFalse(bookProjection.authorNames().isEmpty());
                    Assertions.assertEquals("Belén Velez", bookProjection.authorNames().get(0));
                    Assertions.assertEquals("Marco Salvador", bookProjection.authorNames().get(1));
                })
                .verifyComplete();
    }

    @Test
    void givenBookWithNoAuthors_whenFindById_thenReturnsBookWithoutAuthors() {
        this.bookAuthorDao.findBookWithTheirAuthorsByBookId(3)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("El zorro de arriba y el zorro de abajo", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("2002-05-06"), bookProjection.publicationDate());
                    Assertions.assertFalse(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void givenNonExistingBookId_whenFind_thenOnlyComplete() {
        this.bookAuthorDao.findBookWithTheirAuthorsByBookId(5)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldReturnBookProjectionsFilteredByQuery() {
        var criteria = new BookCriteria("ri", null);
        Pageable pageable = PageRequest.of(0, 5);
        this.bookAuthorDao.findAllToPage(criteria, pageable)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("El zorro de arriba y el zorro de abajo", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("2002-05-06"), bookProjection.publicationDate());
                    Assertions.assertFalse(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("La ciudad y los perros", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1985-03-18"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertFalse(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnBookProjectionsFilteredByPublicationDate() {
        var criteria = new BookCriteria("", LocalDate.parse("1988-07-15"));
        Pageable pageable = PageRequest.of(0, 5);
        this.bookAuthorDao.findAllToPage(criteria, pageable)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("Redoble por Rancas", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1988-07-15"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnBookProjectionsFilteredByQueryAndPublicationDate() {
        var criteria = new BookCriteria("ciu", LocalDate.parse("1985-03-18"));
        Pageable pageable = PageRequest.of(0, 5);
        this.bookAuthorDao.findAllToPage(criteria, pageable)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("La ciudad y los perros", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1985-03-18"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertFalse(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnBookProjectionsWithoutFilter() {
        var criteria = new BookCriteria(" ", null);
        Pageable pageable = PageRequest.of(0, 5);
        this.bookAuthorDao.findAllToPage(criteria, pageable)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("El zorro de arriba y el zorro de abajo", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("2002-05-06"), bookProjection.publicationDate());
                    Assertions.assertFalse(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .expectNextCount(1)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("Los ríos profundos", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1999-01-15"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertFalse(bookProjection.authorNames().isEmpty());
                    Assertions.assertEquals(2, bookProjection.authorNames().size());
                })
                .expectNextCount(1)
                .verifyComplete();
    }

}
````

## 🧪 `BookServiceImplTest`

Esta clase contiene pruebas de integración que validan el comportamiento de alto nivel del servicio `BookService`, el
cual encapsula la lógica de negocio relacionada con la gestión de libros y sus autores. Las pruebas verifican:

- Lógica de persistencia (guardar, actualizar, eliminar).
- Validación de reglas de negocio (autores inexistentes, libros no encontrados).
- Integración con DAO, repositorios y relaciones Book <-> Author.

### ✅ Anotaciones explicadas

| Anotación              | Propósito                                                                                                                                                                           |
|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@Slf4j`               | Proporciona un `log` para imprimir mensajes de depuración (`log.info(...)`).                                                                                                        |
| `@SpringBootTest`      | Carga **el contexto completo de la aplicación** (servicios, repositorios, DAOs, configuración de base de datos, etc.). <br>Se usa cuando se necesita probar múltiples capas juntas. |
| `@Autowired`           | Inyecta el bean `BookService` real desde el contexto de Spring para usarlo en las pruebas.                                                                                          |
| `extends AbstractTest` | Permite que cada test se ejecute con datos predefinidos, cargados desde archivos SQL (`data.sql`, `reset_test_data.sql`), garantizando un entorno controlado y consistente.         |

````java

@Slf4j
@SpringBootTest
class BookServiceImplTest extends AbstractTest {

    @Autowired
    private BookService bookService;

    @Test
    void findAllBooks() {
        this.bookService.findAllBooks()
                .doOnNext(bookResponse -> log.info("{}", bookResponse))
                .as(StepVerifier::create)
                .assertNext(bookResponse -> {
                    Assertions.assertEquals(1, bookResponse.id());
                    Assertions.assertEquals("Los ríos profundos", bookResponse.title());
                    Assertions.assertEquals(LocalDate.parse("1999-01-15"), bookResponse.publicationDate());
                    Assertions.assertTrue(bookResponse.onlineAvailability());
                })
                .assertNext(bookResponse -> Assertions.assertTrue(bookResponse.onlineAvailability()))
                .assertNext(bookResponse -> Assertions.assertFalse(bookResponse.onlineAvailability()))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findBookById() {
        this.bookService.findBookById(1)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("Los ríos profundos", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1999-01-15"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertFalse(bookProjection.authorNames().isEmpty());
                    Assertions.assertEquals(2, bookProjection.authorNames().size());
                })
                .verifyComplete();
    }

    @Test
    void givenNonExistingBookId_whenDeleting_thenThrowsError() {
        this.bookService.findBookById(5)
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(BookNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("El libro [id=5] no fue encontrado", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void findBooksWithAuthorsByCriteria_withData() {
        String query = "ri";
        int pageNumber = 0;
        int pageSize = 2;

        Mono<Page<BookProjection>> result = this.bookService.findBooksWithAuthorsByCriteria(query, null, pageNumber, pageSize)
                .doOnNext(bookProjections -> log.info("{}", bookProjections.getContent()));

        StepVerifier.create(result)
                .assertNext(page -> {
                    Assertions.assertNotNull(page);
                    Assertions.assertEquals(2, page.getContent().size());
                    Assertions.assertEquals(0, page.getNumber());
                    Assertions.assertEquals(2, page.getSize());
                    Assertions.assertEquals(2, page.getTotalElements());
                    Assertions.assertTrue(page.isFirst());
                    Assertions.assertTrue(page.isLast());

                    BookProjection firstBookProjection = page.getContent().get(0);
                    Assertions.assertNotNull(firstBookProjection.title());
                    Assertions.assertNotNull(firstBookProjection.publicationDate());
                    Assertions.assertFalse(firstBookProjection.onlineAvailability());
                    Assertions.assertNull(firstBookProjection.authors());
                    Assertions.assertTrue(firstBookProjection.authorNames().isEmpty());

                    BookProjection secondBookProjection = page.getContent().get(1);
                    Assertions.assertNotNull(secondBookProjection.title());
                    Assertions.assertNotNull(secondBookProjection.publicationDate());
                    Assertions.assertTrue(secondBookProjection.onlineAvailability());
                    Assertions.assertNotNull(secondBookProjection.authors());
                    Assertions.assertFalse(secondBookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void findBooksWithAuthorsByCriteria_withoutData() {
        int pageNumber = 0;
        int pageSize = 2;

        Mono<Page<BookProjection>> result = this.bookService.findBooksWithAuthorsByCriteria(" ", LocalDate.now(), pageNumber, pageSize);

        StepVerifier.create(result)
                .assertNext(page -> {
                    Assertions.assertTrue(page.getContent().isEmpty());
                    Assertions.assertEquals(0, page.getTotalElements());
                    Assertions.assertTrue(page.isFirst());
                    Assertions.assertTrue(page.isLast());
                })
                .verifyComplete();
    }

    @Test
    void givenValidBookRequestWithAuthors_whenSave_thenReturnBookProjection() {
        this.bookService.saveBook(new BookRequest("Spring WebFlux", LocalDate.now(), true, List.of(2, 3, 4)))
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertNotNull(bookProjection.title());
                    Assertions.assertNotNull(bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertEquals(3, bookProjection.authorNames().size());
                })
                .verifyComplete();
    }

    @Test
    void givenBookRequestWithoutAuthors_whenSave_thenReturnBookProjection() {
        this.bookService.saveBook(new BookRequest("Spring WebFlux", LocalDate.now(), true, null))
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertNotNull(bookProjection.title());
                    Assertions.assertNotNull(bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void givenBookRequestWithNonExistingAuthor_whenSave_thenThrowError() {
        this.bookService.saveBook(new BookRequest("Spring WebFlux", LocalDate.now(), true, List.of(2, 4, 10)))
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(AuthorIdsNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("Algunos IDs de autores no existen en el sistema", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void givenExistingBook_whenUpdate_thenReturnUpdatedProjection() {
        this.bookService.updateBook(3, new BookUpdateRequest("Kafka", LocalDate.now(), true))
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("Kafka", bookProjection.title());
                    Assertions.assertEquals(LocalDate.now(), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void givenNonExistingBookId_whenUpdate_thenThrowError() {
        this.bookService.updateBook(5, new BookUpdateRequest("Kafka", LocalDate.now(), true))
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(BookNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("El libro [id=5] no fue encontrado", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void givenNonExistingBookId_whenUpdateAuthors_thenThrowError() {
        this.bookService.updateBookAuthors(5, List.of())
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(BookNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("El libro [id=5] no fue encontrado", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void givenBookIdAndEmptyAuthors_whenUpdateAuthors_thenRemoveAllRelations() {
        this.bookService.updateBookAuthors(1, List.of())
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertNotNull(bookProjection.title());
                    Assertions.assertNotNull(bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void givenBookIdAndValidAuthors_whenUpdateAuthors_thenUpdateRelations() {
        this.bookService.updateBookAuthors(1, List.of(3, 4, 2))
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertNotNull(bookProjection.title());
                    Assertions.assertNotNull(bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertEquals(3, bookProjection.authorNames().size());
                })
                .verifyComplete();
    }


    @Test
    void givenBookIdAndNonExistingAuthors_whenUpdateAuthors_thenThrowError() {
        this.bookService.updateBookAuthors(1, List.of(3, 4, 2, 5))
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(AuthorIdsNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("Algunos IDs de autores no existen en el sistema", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void deleteBookWithoutAssociatedAuthors() {
        this.bookService.deleteBook(3)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void deleteBookWithAssociatedBooks() {
        this.bookService.deleteBook(1)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void throwErrorWhenDeleteBookWithIdThatDoesNotExist() {
        this.bookService.deleteBook(5)
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(BookNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("El libro [id=5] no fue encontrado", throwable.getMessage());
                })
                .verify();
    }
}
````

## 🧪 `BookControllerTest`

Esta clase contiene pruebas de integración del controlador HTTP que expone la API REST para la gestión de libros.
Valida el comportamiento completo del endpoint `/api/v1/books`, incluyendo validaciones, errores, paginación,
persistencia y respuestas.

### ✅ Anotaciones explicadas

| Anotación                     | Propósito                                                                                                               |
|-------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| `@Slf4j`                      | Proporciona un logger `log` para depurar respuestas y errores (`log.info(...)`).                                        |
| `@SpringBootTest`             | Carga todo el contexto de Spring Boot, ideal para pruebas que necesitan controladores, servicios y repositorios reales. |
| `@AutoConfigureWebTestClient` | Activa y configura automáticamente un `WebTestClient` para probar endpoints de forma no bloqueante (reactiva).          |
| `extends AbstractTest`        | Prepara datos iniciales consistentes desde SQL, asegurando que todas las pruebas se ejecuten en un estado controlado.   |

````java

@Slf4j
@AutoConfigureWebTestClient //Para autoconfigurar WebTestClient
@SpringBootTest
class BookControllerTest extends AbstractTest {

    private static final String BOOKS_URI = "/api/v1/books";

    @Autowired
    private WebTestClient client;

    @Test
    void getBooks() {
        this.client.get()
                .uri(BOOKS_URI.concat("/stream"))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(BookResponse.class)
                .getResponseBody()
                .doOnNext(bookResponse -> log.info("{}", bookResponse))
                .as(StepVerifier::create)
                .assertNext(bookResponse -> {
                    Assertions.assertEquals(1, bookResponse.id());
                    Assertions.assertEquals("Los ríos profundos", bookResponse.title());
                    Assertions.assertEquals(LocalDate.parse("1999-01-15"), bookResponse.publicationDate());
                    Assertions.assertTrue(bookResponse.onlineAvailability());
                })
                .assertNext(bookResponse -> {
                    Assertions.assertEquals(2, bookResponse.id());
                    Assertions.assertEquals("La ciudad y los perros", bookResponse.title());
                    Assertions.assertEquals(LocalDate.parse("1985-03-18"), bookResponse.publicationDate());
                    Assertions.assertTrue(bookResponse.onlineAvailability());
                })
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getAuthor() {
        this.client.get()
                .uri(BOOKS_URI.concat("/{bookId}"), 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Los ríos profundos")
                .jsonPath("$.publicationDate").isEqualTo(LocalDate.parse("1999-01-15"))
                .jsonPath("$.onlineAvailability").isEqualTo(true)
                .jsonPath("$.authorNames.length()").isNotEmpty()
                .jsonPath("$.authorNames[0]").isEqualTo("Belén Velez")
                .jsonPath("$.authorNames[1]").isEqualTo("Marco Salvador");
    }

    @Test
    void throwsExceptionWhenSearchingForAnBookWhoseIdDoesNotExist() {
        this.client.get()
                .uri(BOOKS_URI.concat("/{bookId}"), 10)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Libro no encontrado")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("El libro [id=10] no fue encontrado");
    }

    @Test
    void getPaginatedBooks() {
        this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOKS_URI.concat("/paginated"))
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 2)
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.number").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(4);
    }

    @Test
    void getPaginatedBooksWithAuthors() {
        this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOKS_URI.concat("/paginated"))
                        .queryParam("query", "ro")
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 5)
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(3)
                .jsonPath("$.number").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(5)
                .jsonPath("$.totalElements").isEqualTo(3);
    }

    @Test
    void getPaginatedBooksWithoutAuthors() {
        this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOKS_URI.concat("/paginated"))
                        .queryParam("publicationDate", LocalDate.parse("1988-07-15"))
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 5)
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.number").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(5)
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    void givenValidBookRequest_whenSaveBook_thenReturnsCreatedBookProjection() {
        BookRequest bookRequest = new BookRequest("Kubernetes", LocalDate.now(), null, List.of(2, 4));
        this.client.post()
                .uri(BOOKS_URI)
                .bodyValue(bookRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Kubernetes")
                .jsonPath("$.publicationDate").isEqualTo(LocalDate.now())
                .jsonPath("$.onlineAvailability").isEqualTo(false)
                .jsonPath("$.authorNames.length()").isNotEmpty()
                .jsonPath("$.authorNames[0]").isEqualTo("Marco Salvador")
                .jsonPath("$.authorNames[1]").isEqualTo("Luis Sánchez");
    }

    @Test
    void givenInvalidBookRequest_whenSaveBook_thenReturnsValidationErrors() {
        List<Integer> authorIds = new ArrayList<>();
        authorIds.add(1);
        authorIds.add(null);
        authorIds.add(4);
        BookRequest bookRequest = new BookRequest(" ", null, false, authorIds);
        this.client.post()
                .uri(BOOKS_URI)
                .bodyValue(bookRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("El cuerpo de la petición contiene valores no válidos")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("Validation failure")
                .jsonPath("$.errors['authorIds[1]']").isArray()
                .jsonPath("$.errors['authorIds[1]'][0]").isEqualTo("must not be null")
                .jsonPath("$.errors.title").isArray()
                .jsonPath("$.errors.title[0]").exists()
                .jsonPath("$.errors.title[1]").isNotEmpty()
                .jsonPath("$.errors.publicationDate").isArray()
                .jsonPath("$.errors.publicationDate[0]").isEqualTo("must not be null");
    }

    @Test
    void givenBookRequestWithNonExistingAuthors_whenSaveBook_thenThrowsAuthorIdsNotFoundException() {
        BookRequest bookRequest = new BookRequest("Kubernetes", LocalDate.now(), true, List.of(2, 10, 4));
        this.client.post()
                .uri(BOOKS_URI)
                .bodyValue(bookRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Autor no encontrado")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("Algunos IDs de autores no existen en el sistema");
    }

    @Test
    void givenValidBookRequest_whenUpdateBook_thenReturnsUpdatedBookProjection() {
        BookUpdateRequest bookUpdateRequest = new BookUpdateRequest("Spring WebFlux", LocalDate.now(), false);
        this.client.put()
                .uri(BOOKS_URI.concat("/{bookId}"), 1)
                .bodyValue(bookUpdateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Spring WebFlux")
                .jsonPath("$.publicationDate").isEqualTo(LocalDate.now())
                .jsonPath("$.onlineAvailability").isEqualTo(false)
                .jsonPath("$.authorNames").isArray()
                .jsonPath("$.authorNames[0]").isEqualTo("Belén Velez")
                .jsonPath("$.authorNames[1]").isEqualTo("Marco Salvador");
    }

    @Test
    void givenInvalidBookRequest_whenUpdateBook_thenReturnsValidationErrors() {
        BookUpdateRequest bookUpdateRequest = new BookUpdateRequest("", null, null);
        this.client.put()
                .uri(BOOKS_URI.concat("/{bookId}"), 1)
                .bodyValue(bookUpdateRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("El cuerpo de la petición contiene valores no válidos")
                .jsonPath("$.detail").isEqualTo("Validation failure")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errors.title").isArray()
                .jsonPath("$.errors.title.length()").isEqualTo(2)
                .jsonPath("$.errors.publicationDate").isArray()
                .jsonPath("$.errors.publicationDate.length()").isEqualTo(1)
                .jsonPath("$.errors.onlineAvailability").isArray()
                .jsonPath("$.errors.onlineAvailability.length()").isEqualTo(1);
    }

    @Test
    void givenNonIntegerBookId_whenUpdateBook_thenReturnsBadRequest() {
        BookUpdateRequest bookUpdateRequest = new BookUpdateRequest("Spring WebFlux", LocalDate.now(), false);
        this.client.put()
                .uri(BOOKS_URI.concat("/{bookId}"), "abc")
                .bodyValue(bookUpdateRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Error de formato de la petición")
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void givenNonExistingBookId_whenUpdateBook_thenReturnsNotFound() {
        BookUpdateRequest bookUpdateRequest = new BookUpdateRequest("Spring WebFlux", LocalDate.now(), false);
        this.client.put()
                .uri(BOOKS_URI.concat("/{bookId}"), 10)
                .bodyValue(bookUpdateRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Libro no encontrado")
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void givenValidAuthorIds_whenUpdateBookAuthors_thenReturnsUpdatedBookProjection() {
        var request = new BookAuthorUpdateRequest(List.of(1, 2, 3));
        this.client.patch()
                .uri(BOOKS_URI.concat("/{bookId}/authors"), 1)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Los ríos profundos")
                .jsonPath("$.publicationDate").isEqualTo(LocalDate.parse("1999-01-15"))
                .jsonPath("$.onlineAvailability").isEqualTo(true)
                .jsonPath("$.authorNames").isArray()
                .jsonPath("$.authorNames[0]").isEqualTo("Belén Velez")
                .jsonPath("$.authorNames[1]").isEqualTo("Marco Salvador")
                .jsonPath("$.authorNames[2]").isEqualTo("Greys Briones");
    }

    @Test
    void givenNonExistingBookId_whenUpdateBookAuthors_thenReturnsNotFound() {
        var request = new BookAuthorUpdateRequest(List.of(1, 2, 3));
        this.client.patch()
                .uri(BOOKS_URI.concat("/{bookId}/authors"), 10)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Libro no encontrado")
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void givenNullAuthorIdInList_whenUpdateBookAuthors_thenReturnsValidationErrors() {
        List<Integer> authorIds = new ArrayList<>();
        authorIds.add(1);
        authorIds.add(null);
        authorIds.add(4);
        var request = new BookAuthorUpdateRequest(authorIds);
        this.client.patch()
                .uri(BOOKS_URI.concat("/{bookId}/authors"), 1)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("El cuerpo de la petición contiene valores no válidos")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("Validation failure")
                .jsonPath("$.errors['authorIds[1]']").isArray()
                .jsonPath("$.errors['authorIds[1]'][0]").isEqualTo("must not be null");
    }

    @Test
    void givenExistingBookId_whenDeleteBook_thenReturnsNoContent() {
        this.client.delete()
                .uri(BOOKS_URI.concat("/{bookId}"), 1)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void givenNonExistingBookId_whenDeleteBook_thenReturnsNotFound() {
        this.client.delete()
                .uri(BOOKS_URI.concat("/{bookId}"), 5)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Libro no encontrado")
                .jsonPath("$.status").isEqualTo(404);
    }
}
````