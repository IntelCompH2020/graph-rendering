package gr.cite.intelcomp.graphexplorer.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
        "gr.cite.intelcomp.graphexplorer",
		"gr.cite.tools",
		"gr.cite.commons"})
@EntityScan({
		"gr.cite.intelcomp.graphexplorer.data"})
public class GraphExplorer {

	public static void main(String[] args) {
		SpringApplication.run(GraphExplorer.class, args);
	}

}
