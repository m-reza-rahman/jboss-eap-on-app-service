package cafe.web.view;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

import cafe.model.entity.Coffee;

@Named
@SessionScoped
public class Cafe implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final String BASE_URI = "http://localhost/rest/coffees";

	private transient Client client;

	@NotBlank
	protected String name;
	@NotNull
	@PositiveOrZero
	protected BigDecimal price;
	protected List<Coffee> coffeeList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getPrice() {
		return price;
	}

	public void setPrice(Number price) {
		this.price = new BigDecimal(price.toString());
	}

	public List<Coffee> getCoffeeList() {
		return coffeeList;
	}

	@PostConstruct
	private void init() {
		try {
			this.client = ClientBuilder.newClient();
			this.getAllCoffees();
		} catch (IllegalArgumentException | NullPointerException | WebApplicationException ex) {
			logger.severe("Processing of HTTP response failed.");
			ex.printStackTrace();
		}
	}

	private void getAllCoffees() {
		this.coffeeList = this.client.target(BASE_URI).path("/").request(MediaType.APPLICATION_JSON)
				.get(new GenericType<List<Coffee>>() {
				});
	}

	public void addCoffee() {
		Coffee coffee = new Coffee(this.name, this.price.doubleValue());
		this.client.target(BASE_URI).request(MediaType.APPLICATION_JSON).post(Entity.json(coffee));
		this.name = null;
		this.price = null;
		this.getAllCoffees();
	}

	public void removeCoffee(String coffeeId) {
		this.client.target(BASE_URI).path(coffeeId).request().delete();
		this.getAllCoffees();
	}
}
