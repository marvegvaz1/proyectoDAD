package es.us.lsi.dad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ActuadorEntityListWrapper {
	
	private List<ActuadorEntity> actuadorList;

	public ActuadorEntityListWrapper() {
		super();
	}

	public ActuadorEntityListWrapper(Collection<ActuadorEntity> actuadorList) {
		super();
		this.actuadorList = new ArrayList<ActuadorEntity>(actuadorList);
	}
	
	public ActuadorEntityListWrapper(List<ActuadorEntity> actuadorList) {
		super();
		this.actuadorList = new ArrayList<ActuadorEntity>(actuadorList);
	}

	public List<ActuadorEntity> getActuadorList() {
		return actuadorList;
	}

	public void setActuadorList(List<ActuadorEntity> actuadorList) {
		this.actuadorList = actuadorList;
	}

	@Override
	public int hashCode() {
		return Objects.hash(actuadorList);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActuadorEntityListWrapper other = (ActuadorEntityListWrapper) obj;
		return Objects.equals(actuadorList, other.actuadorList);
	}
	
	
	
}
