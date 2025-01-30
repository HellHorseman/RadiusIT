package org.example;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jdbc.JdbcComponent;
import org.apache.commons.dbcp2.BasicDataSource;

import java.util.Random;


public class Application {
    public static void main(String[] args) throws Exception {
        // Создаем CamelContext
        CamelContext context = new DefaultCamelContext();

        // Настраиваем DataSource для PostgreSQL
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/radius");
        dataSource.setUsername("Radius");
        dataSource.setPassword("12345");

        // Регистрируем компонент JDBC в Camel
        JdbcComponent jdbc = new JdbcComponent();
        jdbc.setDataSource(dataSource);
        context.addComponent("jdbc", jdbc);

        // Определяем маршруты
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                // Маршрут для создания таблицы (если не существует)
                from("direct:initDB")
                        .to("jdbc:dataSource")
                        .log("Результат создания таблицы (initDB): ${body}");

                // Каждые 3 секунды вставляем случайные значения
                from("timer://insertRandom?period=3000")
                        .process(exchange -> {
                            int randomValue = new Random().nextInt(100); // 0..99
                            String sql = "INSERT INTO consumption (created_at, value) VALUES (NOW(), " + randomValue + ")";
                            exchange.getIn().setBody(sql);
                        })
                        .to("jdbc:dataSource")
                        .log("Вставлено случайное значение потребления (кВт): ${body}");

                // Каждые 5 секунд получаем последние 5 записей из таблицы
                from("timer://fetchData?period=5000")
                        .setBody(constant("SELECT id, created_at, value FROM consumption ORDER BY id DESC LIMIT 5"))
                        .to("jdbc:dataSource")
                        .log("Последние 5 записей: ${body}");
            }
        });

        // Запускаем контекст
        context.start();

        // Создаём таблицу, если её нет
        context.createProducerTemplate().sendBody(
                "direct:initDB",
                "CREATE TABLE IF NOT EXISTS consumption (" +
                        "   id SERIAL PRIMARY KEY," +
                        "   created_at TIMESTAMP NOT NULL," +
                        "   value INT NOT NULL" +
                        ")"
        );

        // Даем поработать 30 секунд
        Thread.sleep(30000);

        // Останавливаем контекст и завершаем приложение
        context.stop();
    }
}