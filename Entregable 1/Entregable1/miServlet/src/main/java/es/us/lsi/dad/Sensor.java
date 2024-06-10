package es.us.lsi.dad;

import java.math.BigInteger;
import java.util.Objects;

public class Sensor {
	
	private Integer idvalue;
	private Integer idsensor;
	private BigInteger timestamp;
	private Double valueTemp;
	private Double valueHum;
	private Integer idgrupo;
	private Integer idplaca;

	public Sensor(Integer idvalue, Integer idsensor, BigInteger timestamp, Double valueTemp, Double valueHum, Integer idgrupo, Integer idplaca) {
		super();
		this.idvalue = idvalue;
		this.idsensor = idsensor;
		this.timestamp = timestamp;
		this.valueTemp = valueTemp;
		this.valueHum = valueHum;
		this.idgrupo = idgrupo;
		this.idplaca = idplaca;
	}

	public Integer getIdvalue() {
		return idvalue;
	}

	public void setIdvalue(Integer idvalue) {
		this.idvalue = idvalue;
	}

	public Integer getIdsensor() {
		return idsensor;
	}

	public void setIdsensor(Integer idsensor) {
		this.idsensor = idsensor;
	}

	public BigInteger getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(BigInteger timestamp) {
		this.timestamp = timestamp;
	}

	public Double getValueTemp() {
		return valueTemp;
	}

	public void setValueTemp(Double valueTemp) {
		this.valueTemp = valueTemp;
	}
	
	public Double getValueHum() {
		return valueTemp;
	}

	public void setValueHum(Double valueHum) {
		this.valueHum = valueHum;
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
		return Objects.hash(idgrupo, idplaca, idsensor, idvalue, timestamp, valueTemp, valueHum);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sensor other = (Sensor) obj;
		return Objects.equals(idgrupo, other.idgrupo) && Objects.equals(idplaca, other.idplaca)
				&& Objects.equals(idsensor, other.idsensor) && Objects.equals(idvalue, other.idvalue)
				&& Objects.equals(timestamp, other.timestamp) && Objects.equals(valueTemp, other.valueTemp) && Objects.equals(valueHum, other.valueHum);
	}

	@Override
	public String toString() {
		return "Sensor [idvalue=" + idvalue + ", idsensor=" + idsensor + ", timestamp=" + timestamp + ", valueTemp=" + valueTemp + ", valueHum=" + valueHum
				+ ", idgrupo=" + idgrupo + ", idplaca=" + idplaca + "]";
	}

}
