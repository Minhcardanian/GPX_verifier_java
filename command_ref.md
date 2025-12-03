1. MySQL: start & log in

From WSL:

# Start MySQL (if not already running)
sudo service mysql start

# Log in as the app user
mysql -u attempt_user -p
2. Inside MySQL: select DB, inspect table, wipe rows, view rows

Once you see the mysql> prompt:

-- 1) Use the correct database
USE attempt_verifier_db;

-- 2) List all tables
SHOW TABLES;

-- 3) Describe the structure of the attempts table
DESCRIBE attempts;

-- 4) Check how many rows you have
SELECT COUNT(*) FROM attempts;

-- 5) Show recent records (all columns, last 10 by id)
SELECT *
FROM attempts
ORDER BY id DESC
LIMIT 10;

-- 6) Or show a more focused view (id, runner, key metrics)
SELECT
    id,
    runner_id,
    distance_km,
    elevation_gain_m,
    coverage_ratio,
    max_deviation_m,
    difficulty_score,
    result,
    message,
    timestamp
FROM attempts
ORDER BY id DESC
LIMIT 10;

-- 7) Delete ALL rows in attempts (reset table content, keep schema)
DELETE FROM attempts;

-- 8) Confirm it’s empty
SELECT COUNT(*) FROM attempts;

-- 9) Exit MySQL
EXIT;

3. Shell script: set official route GPX

From the project root (GPX_verifier_java/):

# Make sure script is executable (only once needed)
chmod +x set_official_route.sh

# Use a specific GPX as the new official route
./set_official_route.sh gpx/google_gpx/gg_main.gpx
# or
./set_official_route.sh gpx/VMM2025_70K.gpx
./set_official_route.sh gpx/VMM2025_50K.gpx


That copies the chosen file to:

src/main/resources/gpx/route_official.gpx


Then rerun the app:

mvn -q -DskipTests compile
mvn spring-boot:run


You’re then ready to upload GPXs and check new records with the SQL commands above.