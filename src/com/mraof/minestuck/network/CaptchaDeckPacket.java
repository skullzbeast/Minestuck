package com.mraof.minestuck.network;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.EnumSet;

import com.mraof.minestuck.editmode.ServerEditHandler;
import com.mraof.minestuck.inventory.captchalouge.CaptchaDeckHandler;
import com.mraof.minestuck.inventory.captchalouge.ContainerCaptchaDeck;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.relauncher.Side;

public class CaptchaDeckPacket extends MinestuckPacket
{
	
	public static final byte DATA = 0;
	public static final byte MODUS = 1;
	public static final byte CAPTCHALOUGE = 2;
	public static final byte GET = 3;
	
	public byte type;
	
	public NBTTagCompound nbt;
	
	public int itemIndex;
	public boolean getCard;
	
	public CaptchaDeckPacket()
	{
		super(Type.CAPTCHA);
	}

	@Override
	public MinestuckPacket generatePacket(Object... data)
	{
		byte type = (Byte) data[0];
		this.data.writeByte(type);	//Packet type
		if(data.length > 1)
		{
			if(type == DATA)	//Server side data
			{
				try
				{
					this.data.writeBytes(CompressedStreamTools.compress((NBTTagCompound)data[1]));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			else if(type == GET)
			{
				this.data.writeInt((Integer)data[1]);	//Client side index
				this.data.writeBoolean((Boolean)data[2]);	//Retrive card
			}
		}
		
		return this;
	}

	@Override
	public MinestuckPacket consumePacket(ByteBuf data)
	{
		this.type = data.readByte();
		
		if(data.readableBytes() > 0)
		{
			if(this.type == DATA)
			{
				byte[] bytes = new byte[data.readableBytes()];
				data.readBytes(bytes);
				try
				{
					this.nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			else if(this.type == GET)
			{
				this.itemIndex = data.readInt();
				this.getCard = data.readBoolean();
			}
		}
		
		return this;
	}

	@Override
	public void execute(EntityPlayer player)
	{
		if(!player.worldObj.isRemote)
		{
			if(ServerEditHandler.getData(player.getCommandSenderName()) != null)
				return;
			
			if(this.type == MODUS && player.openContainer instanceof ContainerCaptchaDeck)
				CaptchaDeckHandler.useItem((EntityPlayerMP) player);
			else if(this.type == CAPTCHALOUGE && player.getCurrentEquippedItem() != null)
				CaptchaDeckHandler.captchalougeItem((EntityPlayerMP) player);
			else if(this.type == GET)
				CaptchaDeckHandler.getItem((EntityPlayerMP) player, itemIndex, getCard);
		}
		else
		{
			if(this.type == DATA)
			{
				CaptchaDeckHandler.clientSideModus = CaptchaDeckHandler.readFromNBT(nbt, true);
				CaptchaDeckHandler.clientSideModus.getGuiHandler().updateContent();
			}
		}
	}

	@Override
	public EnumSet<Side> getSenderSide()
	{
		return EnumSet.allOf(Side.class);
	}
	
}
