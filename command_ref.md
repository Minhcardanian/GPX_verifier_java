Start MySQL & Log In:
sudo service mysql start
mysql -u attempt_user -p
(password: password123)

Select database:
USE attempt_verifier_db;

List all tables:
SHOW TABLES;

Describe table structure:
DESCRIBE attempts;

Count number of records:
SELECT COUNT(*) FROM attempts;

Show last 10 rows:
SELECT * FROM attempts ORDER BY id DESC LIMIT 10;

Show selected metrics:
SELECT id, runner_id, distance_km, elevation_gain_m, coverage_ratio, max_deviation_m, difficulty_score, result, message, timestamp FROM attempts ORDER BY id DESC LIMIT 10;

Delete all rows (reset table):
DELETE FROM attempts;

Verify deletion:
SELECT COUNT(*) FROM attempts;

Exit MySQL:
EXIT;

Shell script — make executable:
chmod +x set_official_route.sh

Set a new official route (examples):
./set_official_route.sh gpx/google_gpx/gg_main.gpx
./set_official_route.sh gpx/VMM2025_70K.gpx
./set_official_route.sh gpx/VMM2025_50K.gpx

Rebuild & run Spring Boot:
mvn -q -DskipTests compile
mvn spring-boot:run