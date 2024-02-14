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