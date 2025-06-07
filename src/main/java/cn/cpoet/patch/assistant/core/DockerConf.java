package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.AppConst;

/**
 * Docker配置
 *
 * @author CPoet
 */
public class DockerConf implements Cloneable {

    public static final String TYPE_LOCAL = "local";
    public static final String TYPE_REMOTE = "remote";
    public static final String DEFAULT_COMMAND = "docker";
    public static final String DEFAULT_WORK_PATH = "/opt/" + AppConst.APP_NAME;

    /**
     * 类型：local或者remote
     */
    private String type;

    /**
     * 本地命令
     */
    private String localCommand = DEFAULT_COMMAND;

    /**
     * 本地目录
     */
    private String localWorkPath = DEFAULT_WORK_PATH;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private int port = 22;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 命令
     */
    private String command = DEFAULT_COMMAND;

    /**
     * 工作路径
     */
    private String workPath = DEFAULT_WORK_PATH;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocalCommand() {
        return localCommand;
    }

    public void setLocalCommand(String localCommand) {
        this.localCommand = localCommand;
    }

    public String getLocalWorkPath() {
        return localWorkPath;
    }

    public void setLocalWorkPath(String localWorkPath) {
        this.localWorkPath = localWorkPath;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getWorkPath() {
        return workPath;
    }

    public void setWorkPath(String workPath) {
        this.workPath = workPath;
    }

    @Override
    public DockerConf clone() {
        try {
            return (DockerConf) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
