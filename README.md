# Currency Exchanger

REST API для управления валютами и курсами обмена.

## Стек

- Java 17, Jakarta Servlet 6
- SQLite (через JDBC)
- MapStruct, Lombok
- Tomcat 10

## Сборка

```bash
mvn clean package
```

Собирает `target/ROOT.war`.

## Деплой

Скопировать `ROOT.war` в папку `webapps` Tomcat:

```bash
scp target/ROOT.war user@host:/var/lib/tomcat10/webapps/
```

Tomcat развернёт приложение автоматически.


## API

| Метод  | URL                        | Описание                        |
|--------|----------------------------|---------------------------------|
| GET    | /currencies                | Список всех валют               |
| POST   | /currencies                | Добавить валюту                 |
| GET    | /currency/{code}           | Получить валюту по коду         |
| GET    | /exchangeRates             | Список всех курсов              |
| POST   | /exchangeRates             | Добавить курс                   |
| GET    | /exchangeRate/{baseTarget} | Получить курс по паре           |
| PATCH  | /exchangeRate/{baseTarget} | Обновить курс                   |
| GET    | /exchange?from=&to=&amount= | Конвертация валюты             |
