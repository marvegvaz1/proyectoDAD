package es.us.lsi.dad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SensorEntityListWrapper {

	private List<SensorEntity> sensorList;

	public SensorEntityListWrapper() {
		super();
	}

	public SensorEntityListWrapper(Collection<SensorEntity> sensorList) {
		super();
		this.sensorList = new ArrayList<SensorEntity>(sensorList);
	}
	
	public SensorEntityListWrapper(List<SensorEntity> sensorList) {
		super();
		this.sensorList = new ArrayList<SensorEntity>(sensorList);
	}

	public List<SensorEntity> getSensorList() {
		return sensorList;
	}

	public void setSensorList(List<SensorEntity> sensorList) {
		this.sensorList = sensorList;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sensorList);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorEntityListWrapper other = (SensorEntityListWrapper) obj;
		return Objects.equals(sensorList, other.sensorList);
	}
}









