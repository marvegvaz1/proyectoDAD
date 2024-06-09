package es.us.lsi.dad;

import java.math.BigInteger;
import java.util.Objects;

public class SensorEntity {
	
	protected Integer idvalue;
	protected Integer idsensor;
	protected BigInteger timestamp;
	protected Double valueTemp;
	protected Double valueHum;
	protected Integer idgrupo;
	protected Integer idplaca;
	
	public SensorEntity(Integer idvalue, Integer idsensor, BigInteger timestamp, Double valueTemp, Double valueHum, Integer idgrupo, Integer idplaca) {
		super();
		this.idvalue = idvalue;
		this.idsensor = idsensor;
		this.timestamp = timestamp;
		this.valueTemp = valueTemp;
		this.valueHum = valueHum;
		this.idgrupo = idgrupo;
		this.idplaca = idplaca;
	}
	
	public SensorEntity() {
		super();
	}
	
	public Integer getIdValue() {
		return idvalue;
	}

	public void setIdValue(Integer idvalue) {
		this.idvalue = idvalue;
	}

	public Integer getIdSensor() {
		return idsensor;
	}

	public void setIdSensor(Integer idsensor) {
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
		return valueHum;
	}

	public void setValueHum(Double valueHum) {
		this.valueHum = valueHum;
	}
	
	public Integer getIdGrupo() {
		return idgrupo;
	}

	public void setIdGrupo(Integer idgrupo) {
		this.idgrupo = idgrupo;
	}
	
	public Integer getIdPlaca() {
		return idplaca;
	}

	public void setIdPlaca(Integer idplaca) {
		this.idplaca = idplaca;
	}

	@Override
	public String toString() {
		return "SensorEntity [idvalue=" + idvalue + ", idsensor=" + idsensor + ", timestamp="
				+ timestamp + ", valueTemp=" + valueTemp + ", valueHum=" + valueHum + ", idgrupo=" + idgrupo + ", idplaca=" + idplaca + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(idvalue, idsensor, timestamp, valueTemp, valueHum, idgrupo, idplaca);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorEntity other = (SensorEntity) obj;
		return Objects.equals(idvalue, other.idvalue) && Objects.equals(idsensor, other.idsensor) && Objects.equals(timestamp, other.timestamp) 
				&& Objects.equals(valueTemp, other.valueTemp) & Objects.equals(valueHum, other.valueHum) && Objects.equals(idgrupo, other.idgrupo) 
				&& Objects.equals(idplaca, other.idplaca);
	}
	
	

}
