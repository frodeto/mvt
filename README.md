# mvt

Search the Norwegian food composition table ([Matvaretabellen](https://www.matvaretabellen.no) 2015) by nutrient content.

- `MvtInitializer` imports the spreadsheet in `src/main/resources` into MongoDB (or MySQL).
- `Mvt` loads the imported food items and serves a JSON API on embedded Tomcat.

## Requirements

- Java 25 (`.java-version` is set for [jenv](https://www.jenv.be))
- MongoDB, for running the server (see [MongoDB on macOS](#mongodb-on-macos)). The build and tests don't need a database.

Maven is provided by the wrapper (`./mvnw`).

## MongoDB on macOS

Install MongoDB Community Server from MongoDB's Homebrew tap. Recent Homebrew versions only install from taps you have
trusted, so add and trust the tap first:

```bash
brew tap mongodb/brew
```

```bash
brew trust mongodb/brew
```

```bash
brew install mongodb/brew/mongodb-community
```

Start MongoDB as a background service. It then also starts at login:

```bash
brew services start mongodb/brew/mongodb-community
```

Check that it's running with `brew services list`, and stop it with
`brew services stop mongodb/brew/mongodb-community`.

MongoDB listens on `localhost:27017`, which is the app's default `MONGODB_URI`. On Apple Silicon Macs the data is
stored in `/opt/homebrew/var/mongodb` and the log is `/opt/homebrew/var/log/mongodb/mongo.log` (configured in
`/opt/homebrew/etc/mongod.conf`). The install includes the `mongosh` shell; see
[Exploring the data with mongosh](#exploring-the-data-with-mongosh).

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

The server loads all food items at startup. After a new import, restart the server to serve the new data.

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

## Exploring the data with mongosh

Each import is written to a new database named `mvt` + the import time (e.g. `mvt20261026120446`), and older imports
are dropped once it completes. Start the shell:

```bash
mongosh
```

Then find the database and switch to it:

```javascript
show dbs
use mvt20261026120446          // the mvt database listed by show dbs
```

The food items are in the `FOODITEM` collection, and `IMPORT` holds the time the import completed:

```javascript
db.FOODITEM.countDocuments()
db.IMPORT.findOne()

// Look up by name or by the spreadsheet's id
db.FOODITEM.find({ name: /^Laks/ }, { _id: 0, id: 1, mvtId: 1, name: 1 })
db.FOODITEM.findOne({ mvtId: "01.001" })

// The 5 food items with most iron (mg per 100 g)
db.FOODITEM.find({}, { _id: 0, name: 1, "nutrientMap.IRON": 1 }).sort({ "nutrientMap.IRON": -1 }).limit(5)

// Number of food items per product category
db.FOODITEM.aggregate([
    { $group: { _id: "$productSubGroup.productGroup.productCategory", items: { $sum: 1 } } },
    { $sort: { items: -1 } }
])
```

`nutrientMap` keys are the `Nutrient` enum names (`WATER`, `PROTEIN`, `IRON`, ...) and the values are per 100 g, in
the unit shown by `GET /nutrient/{id}`. Nutrients without a known value in the spreadsheet are left out.

Type `exit` to leave the shell. For a one-off query without an interactive session, pass the database in the
connection string:

```bash
mongosh --quiet mongodb://localhost:27017/mvt20261026120446 --eval 'db.FOODITEM.countDocuments()'
```

## License

Apache License 2.0, see [LICENSE](LICENSE).
