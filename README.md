Демонстрационное приложение, которое:

Создаёт таблицу consumption (если она не существует).
Каждые 3 секунды вставляет случайное значение (кВт) в таблицу.
Каждые 5 секунд выводит в лог последние 5 записей из таблицы.
Работает 30 секунд, а затем завершает работу.
Требования
Java 17 
Maven 3+ 
PostgreSQL (установлен и запущен сервер базы данных)

Шаги для запуска
Клонировать репозиторий:
git clone https://github.com/HellHorseman/RadiusIT
cd camel-postgresql-demo

Настроить соединение с базой данных:
В файле Application.java укажите свои параметры:
dataSource.setUrl("jdbc:postgresql://localhost:5432/your_database");
dataSource.setUsername("your_username");
dataSource.setPassword("your_password");
Убедитесь, что в PostgreSQL создана база your_database, а пользователь your_username имеет к ней доступ.

По умолчанию:
dataSource.setUrl("jdbc:postgresql://localhost:5432/radius");
dataSource.setUsername("Radius");
dataSource.setPassword("12345");

Собрать и запустить проект:
mvn clean install
mvn exec:java
