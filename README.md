# Studenti
Mattia Capretti - R334000036

Antonio De Palma - 0334000240

# Train2Gether

Train2Gether è un'applicazione Android per la creazione, gestione ed esecuzione di schede di allenamento.

L'utente può creare le proprie schede, scegliere gli esercizi da un catalogo organizzato per gruppo muscolare, impostare serie, peso, ripetizioni e recuperi, registrare le serie completate durante la sessione e consultare successivamente lo storico degli allenamenti.

## Idea e Innovazione

Train2Gether nasce dall'esigenza di rendere l'allenamento più semplice da seguire e meno dispersivo.

Durante una sessione è facile perdere tempo tra appunti, timer, schede separate o valori da ricordare. L'app riunisce queste operazioni in un unica schermata: la scheda viene preparata prima dell'allenamento e, una volta iniziata la sessione, l'utente deve soltanto concentrarsi sull'esecuzione e segnare le serie completate.

L'obiettivo è favorire organizzazione e continuità, aiutando l'utente a focalizzarsi sull'allenamento e a mantenere una routine costante nel tempo, riducendo quella mancanza di organizzazione che può contribuire alla perdita di motivazione e all'abbandono della palestra.

Tutti i dati rimangono in un database locale e non verranno mai condivisi con terzi.

L'applicazione non chiederà all'utente di fare un login.

## Funzionamento generale

1. L'utente crea una scheda.
2. Aggiunge gli esercizi dal catalogo, filtrandoli per nome o gruppo muscolare.
3. Configura serie, peso, ripetizioni e tempo di recupero.
4. La scheda può essere modificata oppure avviata come allenamento.
5. Durante la sessione vengono registrate le serie completate e viene gestito il timer di recupero.
6. Al termine, tutto l'allenamento compare nello storico.

## Tecnologie principali

* **Kotlin**
* **Android Views e XML**
* **ViewPager2, BottomNavigationView e RecyclerView**
* **Kotlin Coroutines e Flow**
* **Room 2.8.4**
* **KSP**
* **SQLCipher 4.19.0**
* **Android Keystore + AES-256-GCM**

## Struttura Progetto

### Configurazione generale

* `app/build.gradle.kts`: configura SDK e dipendenze principali. Il progetto utilizza `minSdk 26`, `targetSdk 36`, Room con KSP, Material Components e SQLCipher.
* `app/src/main/AndroidManifest.xml`: dichiara `MainActivity`, `AllenamentoActivity` e `DettaglioStoricoActivity`, mantiene le Activity in portrait e disabilita il backup dei dati applicativi.
* `app/src/main/res/values/themes.xml`: definisce il tema Material 3 utilizzato dall'applicazione.

---

## UI Layer

La parte UI può essere presentata seguendo lo stesso percorso che compie l'utente nell'applicazione.

### Navigazione principale

* `MainActivity.kt`: è il punto di ingresso dell'app. Gestisce un `ViewPager2` collegato alla `BottomNavigationView` e permette di passare tra **Home**, dedicata allo storico, e **Allenamento**, dedicata alla gestione delle schede. La classe `ViewPagerAdapter` fornisce al ViewPager i due Fragment e la selezione della barra inferiore viene mantenuta sincronizzata con la pagina visualizzata.
* `activity_main.xml`: contiene `ViewPager2` e `BottomNavigationView`.
* `bottom_nav_menu.xml`: definisce le due destinazioni Home e Allenamento.

### Creazione e gestione delle schede

* `AllenamentoFragment.kt`: mostra tutte le schede dell'utente e permette di crearne una nuova, modificarla, eliminarla oppure avviarla. L'elenco viene osservato in modo reattivo e si aggiorna quando i dati cambiano.
* `fragment_allenamento.xml`: contiene il pulsante **Nuova Scheda** e la RecyclerView delle schede.
* `SchedaAdapter.kt`: visualizza per ogni scheda nome, numero di esercizi e pulsanti **Avvia**, **Modifica** ed **Elimina**, restituendo le azioni al Fragment tramite callback.
* `item_scheda.xml`: definisce la card della singola scheda.

### Modifica ed esecuzione dell'allenamento

* `AllenamentoActivity.kt`: è la schermata centrale dell'app e funziona in due modalità. In **modifica** permette di costruire la scheda, aggiungere esercizi e serie, modificare peso, ripetizioni e recupero e salvare le modifiche. In **allenamento** inizializza la sessione, registra le serie completate, permette di modificare le serie e gestisce il timer di recupero.
* `activity_allenamento.xml`: contiene nome della scheda, timer, lista degli esercizi e pulsanti per aggiungere esercizi e salvare/terminare.
* `SerieEsercizio.kt`: contiene i modelli utilizzati in memoria dalla UI. `SerieEsercizio` conserva numero del set, kg, ripetizioni, stato della checkbox e ID dell'eventuale serie registrata; `EsercizioConSerie` raggruppa esercizio, recupero e lista delle serie.
* `EsercizioAdapter.kt`: gestisce la RecyclerView degli esercizi e, per ogni esercizio, una RecyclerView interna delle serie. Gestisce inoltre il tempo di recupero, l'aggiunta dei set e le operazioni di modifica della struttura della scheda.
* `item_esercizio.xml`: rappresenta nome dell'esercizio, recupero, serie e comandi relativi.
* `SerieAdapter.kt`: gestisce numero del set, kg, ripetizioni e checkbox. Durante l'allenamento la checkbox permette di registrare ciò che è stato realmente completato; in modalità modifica viene nascosta. I `TextWatcher` vengono gestiti tenendo conto del riciclo delle View.
* `item_serie.xml`: definisce graficamente una singola serie.

### Catalogo esercizi

* `CatalogoDialogAdapter.kt`: mostra nel dialog nome e gruppo muscolare degli esercizi e restituisce quello selezionato tramite callback.
* `dialog_selezione_esercizio.xml`: contiene una `SearchView`, un `ChipGroup` e una RecyclerView. I gruppi muscolari vengono ricavati dinamicamente dal catalogo disponibile, evitando una seconda lista hard-coded nell'interfaccia.

### Collegamento tra dati e UI

* `GestoreSchede.kt`: fa da ponte tra la struttura persistente e i modelli utilizzati dalla schermata. Carica una scheda completa, la converte in `SchedaAllenamento`, `EsercizioConSerie` e `SerieEsercizio` e svolge il procedimento inverso quando la scheda deve essere salvata. Activity e Adapter possono quindi lavorare con strutture semplici senza conoscere i dettagli delle relazioni sottostanti.

### Storico

* `HomeFragment.kt`: mostra gli allenamenti completati e apre il dettaglio della sessione selezionata.
* `fragment_home.xml`: contiene la RecyclerView dello storico e il messaggio visualizzato quando non sono presenti allenamenti.
* `StoricoAdapter.kt`: mostra nome della scheda, data, durata, numero di esercizi e numero di serie.
* `item_storico.xml`: definisce la card della singola sessione.
* `DettaglioStoricoActivity.kt`: recupera l'allenamento selezionato, mostra data e durata e raggruppa le serie realmente eseguite per esercizio. Nello stesso file sono presenti gli adapter utilizzati per gli esercizi e per le relative serie.
* `activity_dettaglio_storico.xml`, `item_esercizio_dettaglio.xml` e `item_serie_eseguita.xml`: definiscono la schermata di dettaglio e le righe utilizzate per rappresentare esercizi e serie eseguite.

---

## Data Layer

Il package:

```text
app/src/main/java/com/example/train2gether/data/
```

contiene l'intero livello di persistenza dell'applicazione.

### Perché utilizzare un database relazionale

I dati gestiti da Train2Gether possiedono relazioni naturali tra loro: una scheda contiene più esercizi, ogni esercizio della scheda contiene più serie previste, un allenamento può derivare da una scheda e ogni allenamento contiene le serie realmente eseguite.

```text
Scheda
  └── EsercizioScheda
        └── SeriePrevista

Esercizio
  ├── EsercizioScheda
  └── SerieEseguita

Allenamento
  └── SerieEseguita
```

Un database relazionale permette di rappresentare direttamente questi collegamenti tramite chiavi primarie e foreign key, garantendo integrità referenziale, transazioni e query aggregate. Room fornisce inoltre un'interfaccia Kotlin type-safe sopra SQLite e permette di ottenere aggiornamenti reattivi tramite `Flow`.

### `Entita.kt`

È il file principale del modello persistente e definisce sei entità Room:

* `Esercizio`: esercizio del catalogo con nome e gruppo muscolare.
* `Scheda`: scheda creata dall'utente.
* `EsercizioScheda`: collega esercizio e scheda e conserva ordine e recupero.
* `SeriePrevista`: singola serie di esercizio scheda con peso e ripetizioni programmate.
* `Allenamento`: sessione realmente avviata, con data, durata e scheda di origine.
* `SerieEseguita`: peso e ripetizioni realmente completati.

Le foreign key gestiscono automaticamente le dipendenze. Ad esempio, eliminando una scheda vengono eliminate tramite `CASCADE` le strutture che le appartengono, mentre `Allenamento.schedaIdOrigine` utilizza `SET_NULL` per permettere allo storico di sopravvivere alla cancellazione della scheda originale.

### `RelazioniScheda.kt`

Definisce i modelli composti utilizzati per le schede:

* `SchedaRiepilogo`;
* `EsercizioSchedaCompleto`;
* `SchedaCompleta`;
* `NuovaSeriePrevista`;
* `NuovoEsercizioScheda`.

Le relazioni `@Embedded` e `@Relation` permettono a Room di ricostruire una scheda completa con esercizi e serie mantenendone l'ordine.

### `RelazioniAllenamento.kt`

Definisce:

* `AllenamentoRiepilogo`, utilizzato dalla Home;
* `AllenamentoCompleto`, composto dalla sessione e dalle relative `SerieEseguita`.

Contiene anche le funzioni che ordinano le serie e le raggruppano per esercizio per la schermata di dettaglio.

### `EsercizioDao.kt`

Gestisce il catalogo degli esercizi: inserimento, recupero alfabetico e filtro per gruppo muscolare.

### `SchedaDao.kt`

Gestisce schede, esercizi associati e serie previste.

Le operazioni principali comprendono:

* CRUD delle singole entità;
* `getTutteSchede()`, che restituisce tramite `Flow` il riepilogo delle schede e il numero di esercizi;
* `getSchedaCompleta()`, che recupera una sola scheda con tutte le sue relazioni;
* `creaSchedaCompleta()` e `modificaSchedaCompleta()`, eseguite con `@Transaction`.

Le transazioni garantiscono che un salvataggio composto da più query venga completato interamente oppure annullato in caso di errore.

### `AllenamentoDao.kt`

Gestisce le sessioni e le serie realmente eseguite.

Permette di creare e recuperare un allenamento, inserire/aggiornare/eliminare una serie eseguita, eliminare una sessione abbandonata, recuperare il dettaglio completo e produrre lo storico tramite una query aggregata.

`getStoricoAllenamenti()` restituisce un `Flow`, così la Home si aggiorna automaticamente. `terminaAllenamento()` è transazionale e conclude la sessione soltanto se durata e numero di serie sono validi.

Durante una sessione la persistenza segue ciò che avviene realmente:

```text
checkbox selezionata   → INSERT SerieEseguita
kg/reps modificati     → UPDATE SerieEseguita
checkbox deselezionata → DELETE SerieEseguita
fine allenamento       → UPDATE durata
```

### `DatiIniziali.kt`

Contiene il catalogo predefinito caricato alla prima creazione del database. Gli esercizi sono organizzati in **18 gruppi muscolari**, utilizzati anche dal filtro dinamico della UI.

### `AppDatabase.kt`

È il punto centrale del database Room.

* dichiara tutte le entità;
* espone `EsercizioDao`, `SchedaDao` e `AllenamentoDao`;
* utilizza un singleton per mantenere una sola istanza per processo;
* tramite `RoomDatabase.Callback` inserisce `DatiIniziali` alla prima creazione;
* configura Room affinché il file venga aperto tramite SQLCipher.

---

## Cifratura del database

Una caratteristica importante del progetto è che **il database Room non viene salvato come un normale file SQLite in chiaro**.

Il database viene cifrato tramite **SQLCipher**, mentre la chiave necessaria per aprirlo viene generata casualmente e protetta tramite **Android Keystore**.

### `DatabaseKeyManager.kt`

Alla prima esecuzione:

1. Android Keystore genera una master key **AES-256**;
2. `SecureRandom` genera 32 byte casuali da utilizzare come chiave SQLCipher;
3. la chiave SQLCipher viene cifrata con `AES/GCM/NoPadding`;
4. nelle `SharedPreferences` vengono memorizzati soltanto ciphertext e IV.

La master key non viene hard-coded e rimane gestita dal Keystore.

Agli avvii successivi il processo viene invertito e la chiave SQLCipher viene ricostruita solamente in memoria.

## Risorse

Per lo sviluppo sono state utilizzate come riferimento:

* documentazione ufficiale Android;
* documentazione ufficiale Kotlin;
* documentazione ufficiale Room;
* documentazione ufficiale SQLCipher.

