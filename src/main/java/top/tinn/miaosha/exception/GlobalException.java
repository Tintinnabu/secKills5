package top.tinn.miaosha.exception;

import top.tinn.miaosha.result.CodeMsg;

/**
 * @ClassName GlobalException
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 14:19
 */
public class GlobalException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private CodeMsg codeMsg;

    public GlobalException(CodeMsg codeMsg) {
        super(codeMsg.toString());
        this.codeMsg = codeMsg;
    }

    public CodeMsg getCodeMsg() {
        return codeMsg;
    }
}
