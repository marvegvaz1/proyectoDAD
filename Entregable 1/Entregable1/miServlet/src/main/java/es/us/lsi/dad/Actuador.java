package es.us.lsi.dad;

import java.math.BigInteger;
import java.util.Objects;

public class Actuador {
	
	private Integer idvalue;
	private Integer idactuador;
	private BigInteger timestamp;
	private Double value;
	private Integer idgrupo;
	private Integer idplaca;

	public Actuador(Integer idvalue, Integer idactuador, BigInteger timestamp, Double value, Integer idgrupo, Integer idplaca) {
		super();
		this.idvalue = idvalue;
		this.idactuador = idactuador;
		this.timestamp = timestamp;
		this.value = value;
		this.idgrupo = idgrupo;
		this.idplaca = idplaca;
	}

	public Integer getIdvalue() {
		return idvalue;
	}

	public void setIdvalue(Integer idvalue) {
		this.idvalue = idvalue;
	}

	public Integer getIdactuador() {
		return idactuador;
	}

	public void setIdactuador(Integer idactuador) {
		this.idactuador = idactuador;
	}

	public BigInteger getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(BigInteger timestamp) {
		this.timestamp = timestamp;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Integer getIdgrupo() {
		return idgrupo;
	}

	public void setIdgrupo(Integer idgrupo) {
		this.idgrupo = idgrupo;
	}

	public Integer getIdplaca() {
		return idplaca;
	}

	public void setIdplaca(Integer idplaca) {
		this.idplaca = idplaca;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idactuador, idgrupo, idplaca, idvalue, timestamp, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Actuador other = (Actuador) obj;
		return Objects.equals(idactuador, other.idactuador) && Objects.equals(idgrupo, other.idgrupo)
				&& Objects.equals(idplaca, other.idplaca) && Objects.equals(idvalue, other.idvalue)
				&& Objects.equals(timestamp, other.timestamp) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "Actuador [idvalue=" + idvalue + ", idactuador=" + idactuador + ", timestamp=" + timestamp + ", value="
				+ value + ", idgrupo=" + idgrupo + ", idplaca=" + idplaca + "]";
	}
	
}
