package artillects.entity;

import java.util.List;

import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import artillects.Artillects;
import artillects.Vector3;
import artillects.VectorWorld;
import artillects.hive.ArtillectType;
import artillects.hive.HiveComplex;
import artillects.hive.HiveComplexManager;
import artillects.hive.zone.Zone;

public class EntityArtillectFlying extends EntityFlying implements IArtillect
{
    public int courseChangeCooldown;
    public double waypointX;
    public double waypointY;
    public double waypointZ;
    public Object owner;
    private Zone zone;

    public EntityArtillectFlying(World world)
    {
        super(world);
    }

    @Override
    public void setDead()
    {
        if (this.getOwner() instanceof HiveComplex)
        {
            ((HiveComplex) this.getOwner()).removeDrone(this);
        }
        else if (this.getOwner() instanceof EntityPlayer)
        {
            ((EntityPlayer) this.getOwner()).sendChatToPlayer(ChatMessageComponent.createFromText("One of your drones has died"));
        }
        super.setDead();
    }

    @Override
    protected void updateEntityActionState()
    {
        if (!this.worldObj.isRemote && this.worldObj.difficultySetting == 0)
        {
            this.setDead();
        }

        this.despawnEntity();
        double d0 = this.waypointX - this.posX;
        double d1 = this.waypointY - this.posY;
        double d2 = this.waypointZ - this.posZ;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;

        double groundDistance = 40;

        MovingObjectPosition mop = this.worldObj.rayTraceBlocks_do_do(Vec3.createVectorHelper(this.posX, this.posY, this.posZ), Vec3.createVectorHelper(this.posX, this.posY - 100, this.posZ), false, false);

        if (mop != null && mop.typeOfHit == EnumMovingObjectType.TILE)
        {
            groundDistance = this.posY - mop.blockY;
        }

        if (d3 < 1.0D || d3 > 3600.0D)
        {
            this.waypointX = this.posX + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
            if (groundDistance < 50)
            {
                this.waypointY = this.posY + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
            }
            else
            {
                this.waypointY = this.posY - (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
            }
            this.waypointZ = this.posZ + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
        }

        if (this.courseChangeCooldown-- <= 0)
        {
            this.courseChangeCooldown += this.rand.nextInt(5) + 2;
            d3 = MathHelper.sqrt_double(d3);

            if (this.isCourseTraversable(this.waypointX, this.waypointY, this.waypointZ, d3))
            {
                this.motionX += d0 / d3 * 0.1D;
                this.motionY += d1 / d3 * 0.1D;
                this.motionZ += d2 / d3 * 0.1D;
            }
            else
            {
                this.waypointX = this.posX;
                this.waypointY = this.posY;
                this.waypointZ = this.posZ;
            }
        }
    }

    /** True if the ghast has an unobstructed line of travel to the waypoint. */
    private boolean isCourseTraversable(double par1, double par3, double par5, double par7)
    {
        double d4 = (this.waypointX - this.posX) / par7;
        double d5 = (this.waypointY - this.posY) / par7;
        double d6 = (this.waypointZ - this.posZ) / par7;
        AxisAlignedBB axisalignedbb = this.boundingBox.copy();

        for (int i = 1; i < par7; ++i)
        {
            axisalignedbb.offset(d4, d5, d6);

            if (!this.worldObj.getCollidingBoundingBoxes(this, axisalignedbb).isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public Object getOwner()
    {
        return this.owner;
    }

    @Override
    public void setOwner(Object player)
    {
        this.owner = player;
    }

    @Override
    public Zone getZone()
    {
        return this.zone;
    }

    @Override
    public void setZone(Zone zone)
    {
        this.zone = zone;
    }

    /** Finds a target for the drone to attack */
    public EntityLivingBase getTarget(double range)
    {
        EntityLivingBase entity = null;
        EntityDroneSelector selector = new EntityDroneSelector(this);
        List<EntityLivingBase> entityList = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this.boundingBox.expand(range, range, range));

        double distance = range * range;
        for (EntityLivingBase currentEntity : entityList)
        {
            double d = new Vector3(this).distance(new Vector3(currentEntity));
            if (selector.isEntityApplicable(currentEntity) && d < distance)
            {
                distance = d;
                entity = currentEntity;
            }
        }
        return entity;
    }

    public void launchFireBall(EntityLivingBase targetedEntity, double entityRadius, boolean small)
    {
        double deltaX = targetedEntity.posX - this.posX;
        double deltaY = targetedEntity.boundingBox.minY + targetedEntity.height / 2.0F - (this.posY + this.height / 2.0F);
        double deltaZ = targetedEntity.posZ - this.posZ;
        this.renderYawOffset = this.rotationYaw = -((float) Math.atan2(deltaX, deltaZ)) * 180.0F / (float) Math.PI;

        this.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1008, (int) this.posX, (int) this.posY, (int) this.posZ, 0);
        EntityFireball entitylargefireball = small ? new EntitySmallFireball(this.worldObj, this, deltaX, deltaY, deltaZ) : new EntityLargeFireball(this.worldObj, this, deltaX, deltaY, deltaZ);
        Vec3 vec3 = this.getLook(1.0F);
        entitylargefireball.posX = this.posX + vec3.xCoord * entityRadius;
        entitylargefireball.posY = this.posY + this.height / 2.0F + 0.5D;
        entitylargefireball.posZ = this.posZ + vec3.zCoord * entityRadius;
        this.worldObj.spawnEntityInWorld(entitylargefireball);
    }

    public void fireLaser(EntityLivingBase entity)
    {
        entity.attackEntityFrom(DamageSource.causeMobDamage(this), 5);
        entity.setFire(5);
        Artillects.proxy.renderLaser(this.worldObj, new Vector3(this).add(0, 0.2, 0), new Vector3(entity).add(entity.width / 2, entity.height / 2, entity.width / 2), 1, 0, 0);
    }

    @Override
    public ArtillectType getType()
    {
        return ArtillectType.SEEKER;
    }

    @Override
    public void setType(ArtillectType type)
    {

    }

    @Override
    public IInventory getInventory()
    {
        return null;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setByte("type", (byte) this.getType().ordinal());
        nbt.setBoolean("hive", !(this.getOwner() != HiveComplex.getPlayerHive()));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);

        this.setType(ArtillectType.values()[nbt.getByte("type")]);
        if (!nbt.getBoolean("hive"))
        {
            HiveComplex.getPlayerHive().addDrone(this);
        }
    }

    @Override
    public boolean getCanSpawnHere()
    {
        return HiveComplexManager.instance().getClosestComplex(new VectorWorld(this), 100) != null;
    }

}
