package tbb.touch;

public class IOEvent {

	int type;
	long timestamp;
	int code;
	int value;
	
	public IOEvent(int code,int type, int value, long timestamp){
		this.type=type;
		this.code=code;
		this.value=value;
		this.timestamp=timestamp;
	}
	
	public int getType() {
		return type;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getCode() {
		return code;
	}

	public int getValue() {
		return value;
	}
}
