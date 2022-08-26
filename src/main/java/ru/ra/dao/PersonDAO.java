package ru.ra.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.ra.models.Person;

@Component
public class PersonDAO {

    private final JdbcTemplate jdbcTemplate;

    private static int PEOPLE_COUNT;

    @Autowired
    public PersonDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Person> index() {
        return jdbcTemplate.query("SELECT * FROM Person", new BeanPropertyRowMapper<>(Person.class)); // RowMapper по умолчанию в jdbc
    }

    public Person show(int id) {
        return jdbcTemplate.query("SELECT * FROM Person WHERE id=?", new Object[]{id}, new PersonMapper()) // Можно сделать свой Mapper
                .stream().findAny().orElse(null);
    }

    public void save(Person person) {
        jdbcTemplate.update("INSERT INTO Person(name, age, email) VALUES(?, ?, ?)",
                person.getName(), person.getAge(), person.getEmail());
    }

    public void update(int id, Person updatedPerson) {
        jdbcTemplate.update("UPDATE Person SET name=?, age=?, email=? WHERE id=?",
                updatedPerson.getName(), updatedPerson.getAge(), updatedPerson.getEmail(), id);
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM Person WHERE id=?", id);
//        people.removeIf(person -> person.getId() == id);
    }

    public void testMultipleUpdate() {
        List<Person> people = createOneThousandPeople();
        long before = System.currentTimeMillis();
        for(Person person: people) {
            jdbcTemplate.update("INSERT INTO Person VALUES(?, ?, ?, ?)",
                    person.getId(), person.getName(), person.getAge(), person.getEmail());
        }
        long after = System.currentTimeMillis();
        System.out.println("Time: " + (after-before));
    }

    private List<Person> createOneThousandPeople() {
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            people.add(new Person(i, "Name" + i, 30, "test" + i + "@mail.ru"));
        }
        return people;
    }

    public void testBatchUpdate() {
        List<Person> people = createOneThousandPeople();

        long before = System.currentTimeMillis();
        jdbcTemplate.batchUpdate("INSERT INTO Person VALUES(?, ?, ?, ?)", new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, people.get(i).getId());
                        ps.setString(2, people.get(i).getName());
                        ps.setInt(3, people.get(i).getAge());
                        ps.setString(4, people.get(i).getEmail());
                    }

                    @Override
                    public int getBatchSize() {
                        return people.size();
                    }
                }
                );
        long after = System.currentTimeMillis();
        System.out.println("Time batch update: " + (after-before));
    }
}
