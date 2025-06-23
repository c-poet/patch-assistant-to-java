package cn.cpoet.patch.assistant.model;

/**
 * 打补丁签名信息
 *
 * @author CPoet
 */
public class PatchUpSign extends PatchSign {
    /**
     * 修改统计
     */
    private Integer modTotal;

    /**
     * 删除统计
     */
    private Integer delTotal;

    /**
     * 增加统计
     */
    private Integer addTotal;

    /**
     * 手动删除统计
     */
    private Integer manualDelTotal;

    /**
     * 源应用包md5值
     */
    private String originAppMd5;

    /**
     * 源应用包sha1值
     */
    private String originAppSha1;

    /**
     * 源应用包大小
     */
    private Long originAppSize;

    /**
     * 打补丁的时间
     */
    private String operTime;

    /**
     * 打补丁的操作人员
     */
    private String operUser;

    public Integer getModTotal() {
        return modTotal;
    }

    public void setModTotal(Integer modTotal) {
        this.modTotal = modTotal;
    }

    public Integer getDelTotal() {
        return delTotal;
    }

    public void setDelTotal(Integer delTotal) {
        this.delTotal = delTotal;
    }

    public Integer getAddTotal() {
        return addTotal;
    }

    public void setAddTotal(Integer addTotal) {
        this.addTotal = addTotal;
    }

    public Integer getManualDelTotal() {
        return manualDelTotal;
    }

    public void setManualDelTotal(Integer manualDelTotal) {
        this.manualDelTotal = manualDelTotal;
    }

    public String getOriginAppMd5() {
        return originAppMd5;
    }

    public void setOriginAppMd5(String originAppMd5) {
        this.originAppMd5 = originAppMd5;
    }

    public String getOriginAppSha1() {
        return originAppSha1;
    }

    public void setOriginAppSha1(String originAppSha1) {
        this.originAppSha1 = originAppSha1;
    }

    public Long getOriginAppSize() {
        return originAppSize;
    }

    public void setOriginAppSize(Long originAppSize) {
        this.originAppSize = originAppSize;
    }

    public String getOperTime() {
        return operTime;
    }

    public void setOperTime(String operTime) {
        this.operTime = operTime;
    }

    public String getOperUser() {
        return operUser;
    }

    public void setOperUser(String operUser) {
        this.operUser = operUser;
    }
}
