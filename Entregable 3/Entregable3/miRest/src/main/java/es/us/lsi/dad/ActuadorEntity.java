 package es.us.lsi.dad;

import java.math.BigInteger;
import java.util.Objects;

public class ActuadorEntity {
	
	protected Integer idvalue;
	protected Integer idactuador;
	protected BigInteger timestamp;
	protected Double value;
	protected Integer idgrupo;
	protected Integer idplaca;

	public ActuadorEntity(Integer idvalue, Integer idactuador, BigInteger timestamp, Double value, Integer idgrupo, Integer idplaca) {
		super();
		this.idactuador = idactuador;
		this.timestamp = timestamp;
		this.value = value;
		this.idgrupo = idgrupo;
		this.idplaca = idplaca;
	}
	
	public ActuadorEntity() {
		super();
	}
	
	public Integer getIdValue() {
		return idvalue;
	}

	public void setIdValue(Integer idvalue) {
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
		return "ActuadorEntity [idvalue=" + idvalue + ", idactuador=" + idactuador + ", timestamp=" + timestamp + ", value=" + value + 
				", idgrupo=" + idgrupo + ", idplaca=" + idplaca + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(idvalue, idactuador, timestamp, value, idgrupo, idplaca);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActuadorEntity other = (ActuadorEntity) obj;
		return Objects.equals(idvalue, other.idvalue) && Objects.equals(idactuador, other.idactuador) && Objects.equals(timestamp, other.timestamp) 
				&& Objects.equals(value, other.value) && Objects.equals(idgrupo, other.idgrupo) && Objects.equals(idplaca, other.idplaca);
	}
}
