package top.tinn.miaosha.redis;

public interface KeyPrefix {
		
	int expireSeconds();
	
	String getPrefix();
	
}
