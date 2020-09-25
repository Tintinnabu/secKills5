package top.tinn.miaosha.redis;

public class UserKey extends BasePrefix{

	private UserKey(String prefix) {
		super(prefix);
	}
	//永不过期
	public static UserKey getById = new UserKey("id");
	public static UserKey getByName = new UserKey("name");
}
