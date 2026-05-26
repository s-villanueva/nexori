package com.example.ExamenUno;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.SpringDataWebSettings;

@SpringBootApplication
public class ExamenUnoApplication implements CommandLineRunner {
	@Autowired
    private SpringDataWebSettings springDataWebSettings;

	public static void main(String[] args) {
		SpringApplication.run(ExamenUnoApplication.class, args);

		System.out.println("Hola mundo");
	}

	@Override
	public void run(String... args) throws Exception {
		utilitario();
	}

	private void utilitario() {
//		Prueba prueba = new Prueba();
//		prueba.setNombre("Nueva prueba uwu");
//		pruebaRepository.save(prueba);
	}


}
