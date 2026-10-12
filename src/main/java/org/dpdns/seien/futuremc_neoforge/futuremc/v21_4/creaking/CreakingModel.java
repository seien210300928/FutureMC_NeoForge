package org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import org.dpdns.seien.futuremc_neoforge.futuremc.v21_4.creaking.Creaking;

/**
 * 嘎吱的分层模型。
 * <p>
 * 网格布局对照 26.3 原版的嘎吱模型（64x64 纹理）：根节点下是上半身
 * （头/身体/双臂）与双腿。两个图层定义：完整身体（主体）与仅头部的
 * 眼睛发光层（渲染时叠加在主体上，用发光材质让眼睛在暗处可见）。
 */
public class CreakingModel extends HierarchicalModel<Creaking> {
    /** 角度转弧度系数。 */
    private static final float DEG_TO_RAD = (float) Math.PI / 180;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    /** 是否为仅眼睛的发光层（此时身体部件为 null）。 */
    private final boolean eyesOnly;

    public CreakingModel(ModelPart roots, boolean eyesOnly) {
        this.eyesOnly = eyesOnly;
        this.root = roots.getChild("root");
        ModelPart upperBody = this.root.getChild("upper_body");
        this.head = upperBody.getChild("head");
        if (eyesOnly) {
            this.body = null;
            this.rightArm = null;
            this.leftArm = null;
            this.leftLeg = null;
            this.rightLeg = null;
        } else {
            this.body = upperBody.getChild("body");
            this.rightArm = upperBody.getChild("right_arm");
            this.leftArm = upperBody.getChild("left_arm");
            this.leftLeg = this.root.getChild("left_leg");
            this.rightLeg = this.root.getChild("right_leg");
        }
    }

    /** 返回根节点，供层级动画使用。 */
    @Override
    public ModelPart root() {
        return this.root;
    }

    /** 完整身体的模型图层定义。 */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition upperBody = root.addOrReplaceChild("upper_body", CubeListBuilder.create(), PartPose.offset(-1.0F, -19.0F, 0.0F));
        addHead(upperBody);
        addBodyAndArms(upperBody);
        addLegs(root);
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** 仅眼睛的模型图层定义（只包含头部）。 */
    public static LayerDefinition createEyesLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition upperBody = root.addOrReplaceChild("upper_body", CubeListBuilder.create(), PartPose.offset(-1.0F, -19.0F, 0.0F));
        addHead(upperBody);
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** 添加头部与两侧树冠状的"头发"平面。 */
    private static void addHead(PartDefinition upperBody) {
        upperBody.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -10.0F, -3.0F, 6.0F, 10.0F, 6.0F)
                .texOffs(28, 31).addBox(-3.0F, -13.0F, -3.0F, 6.0F, 3.0F, 6.0F)
                .texOffs(12, 40).addBox(3.0F, -13.0F, 0.0F, 9.0F, 14.0F, 0.0F)
                .texOffs(34, 12).addBox(-12.0F, -14.0F, 0.0F, 9.0F, 14.0F, 0.0F),
            PartPose.offset(-3.0F, -11.0F, 0.0F));
    }

    /** 添加身体与双臂。 */
    private static void addBodyAndArms(PartDefinition upperBody) {
        upperBody.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0.0F, -3.0F, -3.0F, 6.0F, 13.0F, 5.0F)
                .texOffs(24, 0).addBox(-6.0F, -4.0F, -3.0F, 6.0F, 7.0F, 5.0F),
            PartPose.offset(0.0F, -7.0F, 1.0F));
        upperBody.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(22, 13).addBox(-2.0F, -1.5F, -1.5F, 3.0F, 21.0F, 3.0F)
                .texOffs(46, 0).addBox(-2.0F, 19.5F, -1.5F, 3.0F, 4.0F, 3.0F),
            PartPose.offset(-7.0F, -9.5F, 1.5F));
        upperBody.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(30, 40).addBox(0.0F, -1.0F, -1.5F, 3.0F, 16.0F, 3.0F)
                .texOffs(52, 12).addBox(0.0F, -5.0F, -1.5F, 3.0F, 4.0F, 3.0F)
                .texOffs(52, 19).addBox(0.0F, 15.0F, -1.5F, 3.0F, 4.0F, 3.0F),
            PartPose.offset(6.0F, -9.0F, 0.5F));
    }

    /** 添加双腿。 */
    private static void addLegs(PartDefinition root) {
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(42, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F)
                .texOffs(45, 55).addBox(-1.5F, 15.7F, -4.5F, 5.0F, 0.0F, 9.0F),
            PartPose.offset(1.5F, -16.0F, 0.5F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 34).addBox(-3.0F, -1.5F, -1.5F, 3.0F, 19.0F, 3.0F)
                .texOffs(45, 46).addBox(-5.0F, 17.2F, -4.5F, 5.0F, 0.0F, 9.0F)
                .texOffs(12, 34).addBox(-3.0F, -4.5F, -1.5F, 3.0F, 3.0F, 3.0F),
            PartPose.offset(-1.0F, -17.5F, 0.5F));
    }

    /** 动画：移动摆腿摆臂、攻击抬手、受击摇晃、解体后仰。 */
    @Override
    public void setupAnim(Creaking entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!this.eyesOnly) {
            this.body.xRot = 0.0F;
            this.body.yRot = 0.0F;
            this.rightArm.xRot = 0.0F;
            this.rightArm.yRot = 0.0F;
            this.leftArm.xRot = 0.0F;
            this.leftArm.yRot = 0.0F;
            this.leftLeg.xRot = 0.0F;
            this.rightLeg.xRot = 0.0F;
        }
        this.root.xRot = 0.0F;
        this.root.zRot = 0.0F;
        this.head.xRot = headPitch * DEG_TO_RAD;
        this.head.yRot = netHeadYaw * DEG_TO_RAD;
        if (this.eyesOnly) {
            return;
        }
        // 可移动时按行走摆动双腿双臂。
        if (entity.canMove()) {
            float legSwing = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
            this.leftLeg.xRot = legSwing;
            this.rightLeg.xRot = -legSwing;
            this.leftArm.xRot = -legSwing * 0.6F;
            this.rightArm.xRot = legSwing * 0.6F;
        }
        // 攻击：右臂前抬、身体前倾。
        if (entity.attackAnimationState.isStarted()) {
            this.rightArm.xRot = -1.5707964F;
            this.body.xRot = 0.1F;
        }
        // 受击无敌：整体左右摇晃。
        if (entity.invulnerabilityAnimationState.isStarted()) {
            this.root.zRot = Mth.sin(ageInTicks * 0.5F) * 0.05F;
            this.body.xRot = 0.1F;
        }
        // 解体：整体后仰、双臂下垂。
        if (entity.deathAnimationState.isStarted()) {
            this.root.xRot = -0.6F;
            this.leftArm.xRot = -1.2F;
            this.rightArm.xRot = -1.2F;
        }
    }
}
