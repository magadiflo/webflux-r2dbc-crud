# [WebFlux R2DBC Crud using PostgreSQL](https://www.youtube.com/watch?v=s6qKE0FD3BU&t=2137s)

- Proyecto tomado del canal de youtube de `Joas Dev`.
- Este proyecto está actualizado (25/06/2025) con algunos detalles que se vieron en el curso de
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

## Creando el esquema y datos de nuestra base de datos

Creamos el siguiente directorio `src/main/resources/sql` en nuestro proyecto. Aquí creamos el archivo `scheme.sql`
donde definimos las tablas `authors`, `books` y su relación de muchos a muchos `book_authors`.

Definimos las instrucciones `DROP TABLE...` al inicio de este script para que la aplicación inicie limpia y siempre
con los datos iniciales del archivo `data.sql` que crearemos más adelante.

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
    CONSTRAINT fk_authors_book_authors FOREIGN KEY(author_id) REFERENCES authors(id)
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

Si hasta este punto ejecutamos la aplicación, veremos que el log nos muestra una ejecución existosa.

````bash
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.3)

2025-06-25T18:19:31.080-05:00  INFO 6252 --- [webflux-r2dbc-crud] [           main] d.m.r.app.WebfluxR2dbcCrudApplication    : Starting WebfluxR2dbcCrudApplication using Java 21.0.6 with PID 6252 (D:\programming\spring\02.youtube\10.joas_dev\webflux-r2dbc-crud\target\classes started by magadiflo in D:\programming\spring\02.youtube\10.joas_dev\webflux-r2dbc-crud)
2025-06-25T18:19:31.087-05:00  INFO 6252 --- [webflux-r2dbc-crud] [           main] d.m.r.app.WebfluxR2dbcCrudApplication    : No active profile set, falling back to 1 default profile: "default"
2025-06-25T18:19:32.036-05:00  INFO 6252 --- [webflux-r2dbc-crud] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data R2DBC repositories in DEFAULT mode.
2025-06-25T18:19:32.057-05:00  INFO 6252 --- [webflux-r2dbc-crud] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 15 ms. Found 0 R2DBC repository interfaces.
2025-06-25T18:19:33.395-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-25T18:19:33.409-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-25T18:19:33.494-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [      Thread-17] io.r2dbc.postgresql.QUERY                : Executing query: DROP TABLE IF EXISTS book_authors
2025-06-25T18:19:33.502-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: DROP TABLE IF EXISTS authors
2025-06-25T18:19:33.513-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-2] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-25T18:19:33.513-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: DROP TABLE IF EXISTS books
2025-06-25T18:19:33.515-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-2] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-25T18:19:33.518-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: CREATE TABLE authors( id SERIAL, first_name VARCHAR(45) NOT NULL, last_name VARCHAR(45) NOT NULL, birthdate DATE NOT NULL, CONSTRAINT pk_authors PRIMARY KEY(id) )
2025-06-25T18:19:33.536-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: CREATE TABLE books( id SERIAL, title VARCHAR(255) NOT NULL, publication_date DATE NOT NULL, online_availability BOOLEAN DEFAULT FALSE, CONSTRAINT pk_books PRIMARY KEY(id) )
2025-06-25T18:19:33.542-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: CREATE TABLE book_authors( book_id INTEGER NOT NULL, author_id INTEGER NOT NULL, CONSTRAINT fk_books_book_authors FOREIGN KEY(book_id) REFERENCES books(id), CONSTRAINT fk_authors_book_authors FOREIGN KEY(author_id) REFERENCES authors(id) )
2025-06-25T18:19:33.551-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [      Thread-18] io.r2dbc.postgresql.QUERY                : Executing query: INSERT INTO authors(first_name, last_name, birthdate) VALUES ('Milagros', 'Díaz', '2006-06-15'), ('Lesly', 'Águila', '1995-06-09'), ('Kiara', 'Lozano', '2001-10-03'), ('Briela', 'Cirilo', '1997-09-25')
2025-06-25T18:19:33.556-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: INSERT INTO books(title, publication_date, online_availability) VALUES ('Cien años de soledad', '1999-01-15', true), ('Paco Yunque', '1985-03-18', true), ('Los perros hambrientos', '2002-05-06', false), ('Edipo Rey', '1988-07-15', true)
2025-06-25T18:19:33.559-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: INSERT INTO book_authors(book_id, author_id) VALUES (1, 1), (1, 2)
2025-06-25T18:19:33.561-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: INSERT INTO book_authors(book_id, author_id) VALUES (2, 3)
2025-06-25T18:19:33.599-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-3] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-25T18:19:33.601-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-3] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-25T18:19:33.722-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-4] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-25T18:19:33.724-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-4] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-25T18:19:33.837-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-5] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-25T18:19:33.839-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-5] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-25T18:19:34.067-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-6] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-25T18:19:34.069-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-6] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-25T18:19:34.177-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-7] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-25T18:19:34.180-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-7] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-25T18:19:34.291-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-8] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-25T18:19:34.293-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-8] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-25T18:19:34.405-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-25T18:19:34.407-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-1] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-25T18:19:34.608-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-2] io.r2dbc.postgresql.QUERY                : Executing query: SHOW TRANSACTION ISOLATION LEVEL
2025-06-25T18:19:34.610-05:00 DEBUG 6252 --- [webflux-r2dbc-crud] [actor-tcp-nio-2] io.r2dbc.postgresql.QUERY                : Executing query: SELECT oid, * FROM pg_catalog.pg_type WHERE typname IN ('hstore','geometry','vector')
2025-06-25T18:19:34.725-05:00  INFO 6252 --- [webflux-r2dbc-crud] [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 8080 (http)
2025-06-25T18:19:34.755-05:00  INFO 6252 --- [webflux-r2dbc-crud] [           main] d.m.r.app.WebfluxR2dbcCrudApplication    : Started WebfluxR2dbcCrudApplication in 4.455 seconds (process running for 5.096)
````

Y si ahora revisamos la base de datos `db_webflux_r2dbc` en postgres, veremos que las tablas se han creado
y poblado correctamente.

![02.png](assets/02.png)
