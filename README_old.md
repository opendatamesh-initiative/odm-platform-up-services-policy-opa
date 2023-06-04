## Descrizione
Questo progetto contiente un'applicazione Spring Boot per comunicare con un server OPA e, ove sia desiderato, anche il server OPA stesso (i.e., ex progetto *https://bitbucket.org/quantyca/prototipo-opa-dataproduct/src/master/*).

## Requisiti
1. Docker e docker-compose installati sulla macchina host
2. Intellij IDE

## Configurazione di default e Docker
L'ambiente di default è Docker, indicato nel file `application-docker.yml`. Affinchè possa essere utilizzato correttamente sarà necessario avviare l'applicazione, dalla root progettuale, tramite il comando `docker-compose up --b`. 
Per la corretta esecuzione dell'applicativo, che includerà anche una versione Dockerizzata del server OPA necessario per salvare e validare le policy, sarà richiesto un file di environment, ossia `.env`.
Tale file dovrà contenere le seguenti variabili (se ne riporta un esempio pre-popolato):
```
# OPA parameters
OPA_LOCAL_PORT=8181
OPA_DOCKER_PORT=8181
OPA_LOG_LEVEL=debug
# App & Spring parameters
APP_NAME=policyservice-opa
JAVA_OPTS=
SPRING_PROFILES_ACTIVE=docker
SPRING_LOCAL_PORT=4242
SPRING_DOCKER_PORT=4242
# DB & Flyway parameters
DATABASE_URL=jdbc:h2:mem:testdb
DATABASE_USERNAME=sa
DATABASE_PASSWORD=password
SPRING_DATABASE_DIALECT=org.hibernate.dialect.H2Dialect
SPRING_DATABASE_DRIVER=org.h2.Driver
FLYWAY_SCHEMA=flyway
FLYWAY_SCRIPTS_DIR=h2
```

## Avvio applicazione
Se si vuole avviare l'applicazione in locale, per scopi di sviluppo, sarà necessario usare il profilo `dev` (il cui file di properties di riferimento è `application-dev.yml`), ma, soprattutto, bisognerà assicurarsi di avere un server OPA avviato. 
Risulta possibile avviare la versione del solo server OPA dockerizzato come segue:

1. Recarsi a *src/main/resources/opa*
2. Eseguire il comando `docker-compose up --build`

Una volta che si dispone del server OPA sarà possibile avviare l'applicazione come riportato a seguire:

3. Verificare le configurazioni del punto precedente (environment) ed eventualmente adattarle al proprio contesto/ambiente
4. Dalla root del repository lanciare i seguenti comandi docker:
```
docker build -t policyservice-opa --build-arg ARG=<value>  --build-arg ...
docker run --name policyservice-opa -p 4242:4242 -d policyservice-opa
```
dove sarà presente un parametro `--build-arg ARG=<value>` per ogni parametro riportato nell'esempio del file di environment della sezione precedente. 
Inoltre, sarà presente un parametro aggiuntivo, ossia `--build-arg OPA_HOSTNAME=<value>` che nel caso precedente risulta invece trasparente all'utilizzatore.

L'applicazione, oltre alla possibilità di utilizzare H2 come DB, prevede anche la possibilità di avvalersi di MySQL o Postgresql. 
Per entrambe le tecnologie è possibile trovare un file *docker-compose.yml* testato e funzionante presso il path *src/main/resources*. 
Qualora si utilizzasse una delle due tecnologie risulta necessario selezionare il corrispondente profilo Spring Boot.

## Esempio di utilizzo
1. Effettuare una richiesta *GET* all'API /policies e verificare che l'applicazione funzioni e restituisca le policy pre-esistenti in OPA (se creato da zero, nessuna policy)
2. Effettuare una richiesta *PUT* all'API /policies/dataproduct e creare una prima policy (vedi collezione Postman per un possibile body)
3. Effettuare una richiesta *PUT* all'API /policies/xpolicy e creare una seconda policy (vedi collezione Postman per un possibile body)
4. Effettuare una richiesta *GET* all'API /policies e verificare che l'applicazione recuperi entrambe le policy create
5. Effettuare una richiesta *GET* all'API /policies/dataproduct e verificare che l'applicazione recuperi la prima policy creata
6. Effettuare una richiesta *POST* all'API /validate?id=dataproduct (vedi collezione Postman per un possibile body) e verificare che venga applicata la policy
7. Effettuare una richiesta *POST* all'API /validate?id=dataproduct,xpolicy,fakepolicy e verificare che vengano applicate le policy (nessun esito per policy inesistenti)
8. Effettuare una richiesta *POST* all'API /validate e verificare che vengano applicate tutte le policy presenti in OPA
9. Effettuare una richiesta *PUT* all'API /suites (vedi collezione Postman)
10. Effettuare una richiesta *GET* all'API /suites (vedi collezione Postman) e verificare che venga recuperata la suite creata al punto precedente
11. Effettuare una richiesta *POST* all'API /validate?suite=odm-suite e verificare che vengano validate tutte le policy contenute nella suite

## Utilities
* Nella cartella di progetto sono contenute due collezioni Postman contenenti alcune chiamate d'esempio (cartella "Postman")
    * *OPA.postman_collection.json*: collezione di chiamate REST per interagire direttamente con il server OPA
    * *ODM.postman_collection.json*: collezione di chiamate REST per interagire con il progetto come intermediario verso il server OPA
* Le API sono documentate mediante Swagger, raggiungibile di default presso *http://localhost:4242/api/v1/planes/utility/policy-services/opa/swagger-ui/index.html*

## Nota importante
Al momento i body di request e response rispecchiano i formati attesi e prodotti da OPA, da capire se la loro normalizzazione in un formato standard risieda in questo layer o nell'experience layer