$env:DB_HOST="aws-1-eu-central-1.pooler.supabase.com"
$env:DB_PORT="6543"
$env:DB_NAME="postgres"
$env:DB_USER="postgres.uxkdnowkwppueuyjixzj"
$env:DB_PASSWORD="ypU6V%vWZ3zS?nN"

Write-Host "Variables d'environnement chargees pour Supabase."
Write-Host "Lancement du backend Spring Boot en local..."

./mvnw spring-boot:run
