# mvt

Search the Norwegian food composition table ([Matvaretabellen](https://www.matvaretabellen.no) 2015) by nutrient content.

- `MvtInitializer` imports the spreadsheet in `src/main/resources` into MongoDB (or MySQL).
- `Mvt` loads the imported food items and serves a JSON API on embedded Tomcat.

## Requirements

- Java 25 (`.java-version` is set for [jenv](https://www.jenv.be))
- MongoDB, for running the server. The build and tests don't need a database.

Maven is provided by the wrapper (`./mvnw`).

## Build and test

```bash
./mvnw verify
```

## Run

Import the spreadsheet into MongoDB, then start the server:

```bash
./mvnw compile exec:java -Dexec.mainClass=control.MvtInitializer
```

```bash
./mvnw exec:java
```

To import into MySQL instead, pass `-Dexec.args=mysql` to the import and set `MYSQL_PASSWORD`. The database must exist.

### Configuration

| Variable         | Default                      | Used by                |
|------------------|------------------------------|------------------------|
| `PORT`           | `4567`                       | server                 |
| `MONGODB_URI`    | `mongodb://localhost:27017`  | server, Mongo import   |
| `MYSQL_URL`      | `jdbc:mysql://localhost/mvt` | MySQL import           |
| `MYSQL_USER`     | `root`                       | MySQL import           |
| `MYSQL_PASSWORD` | (required)                   | MySQL import           |

## API

| Request                                      | Response                                                        |
|----------------------------------------------|-----------------------------------------------------------------|
| `GET /nutrients`                             | All nutrients: `[{"id":0,"name":"Vann"}, ...]`                  |
| `GET /nutrient/{id}`                         | One nutrient with its English name and unit                     |
| `GET /search?Nutrient={name}&Amount={level}` | Food items with at least `level` of the nutrient (per 100 g)    |
| `GET /fooditem/{id}`                         | One food item with group, category and all known nutrient values |

`Nutrient` is the Norwegian column name from the spreadsheet, e.g. `Protein`, `Jern` or `Vitamin C` (case insensitive).
Invalid input gives `400` and unknown ids give `404`, both with a body like `{"error":"..."}`.

```bash
curl 'http://localhost:4567/search?Nutrient=Jern&Amount=10'
```

## License

Apache License 2.0, see [LICENSE](LICENSE).
