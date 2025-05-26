package cn.cpoet.patch.assistant.core;

/**
 * Docker配置
 *
 * @author CPoet
 */
public class DockerConf implements Cloneable {

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private String port = "22";

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 工作路径
     */
    private String workPath;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
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
