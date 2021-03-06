// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Iterator;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;


public class MCX140 extends BaseIC {
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	private final String TITLE = "IN AREA";
	
	@Override
    public String getTitle() {
    	return "^"+TITLE;
    }

    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
	@Override
    public boolean requiresPermission() {
        return true;
    }

    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     * @return
     */
	@Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
    	
		sign = UtilIC.getSignTextWithExtension(cbworld, pos, sign);
		
    	if(sign.getLine3().isEmpty())
    	{
    		return "3rd line must contain an entity name/type";
    	}
    	else
    	{
    		String[] args = sign.getLine3().split("\\+", 2);
    		if(!UtilEntity.isValidEntityTypeID(args[0]))
    		{
    			return "Invalid name on Line 3";
    		}
    		
    		if(args.length > 1 && !UtilEntity.isValidEntityTypeID(args[1]))
    		{
    			return "Invalid rider name on Line 3";
    		}
    	}
    	
    	if(!sign.getLine4().isEmpty())
    	{
    		String out = UtilIC.isValidDimensions(sign.getLine4(), "4th", 1,16, 1,16, 1,16,   -10,10, -10,10, -10,10);
    		if(out != null)
    			return out;
    	}
    	
        return null;
    }
    
    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip)
    {
    	if(chip.inputAmount() == 0
    		|| (chip.getText().getLine2().charAt(3) == 'X' && chip.getIn(1).isTriggered() && chip.getIn(1).is())
    		)
    	{
	    	int width = 3;
	    	int height = 1;
	    	int length = 3;
	    	int offx = 0;
	    	int offy = 1;
	    	int offz = 0;
	    	
	    	if(!chip.getText().getLine4().isEmpty())
	    	{
	    		SignText text = UtilIC.getSignTextWithExtension(chip);
	    		
	    		String[] args = text.getLine4().split("/", 2);
	    		String[] dim = args[0].split(":", 3);
	    		
	    		if(dim.length != 3)
	    			return;
	    		
	    		width = Integer.parseInt(dim[0]);
	    		height = Integer.parseInt(dim[1]);
	    		length = Integer.parseInt(dim[2]);
	    		
	    		if(args.length > 1)
	    		{
	    			String[] offsets = args[1].split(":", 3);
	    			offx = Integer.parseInt(offsets[0]);
	    			offy = Integer.parseInt(offsets[1]);
	    			offz = Integer.parseInt(offsets[2]);
	    		}
	    	}
	    	
	    	if(width > RedstoneListener.icInAreaMaxLength)
	    		width = RedstoneListener.icInAreaMaxLength;
	    	if(length > RedstoneListener.icInAreaMaxLength)
	    		length = RedstoneListener.icInAreaMaxLength;
	    	
	    	World world = CraftBook.getWorld(chip.getCBWorld());
	    	Vector lever = Util.getWallSignBack(chip.getCBWorld(), chip.getPosition(), 2);
	        int data = CraftBook.getBlockData(world, chip.getPosition());
	        BlockArea area = UtilIC.getBlockArea(chip, data, width, height, length, offx, offy, offz);
	        
	        detectEntity(world, lever, area, chip);
    	}
    	else if(chip.getIn(1).isTriggered())
    	{
    		if(chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '%')
    		{
    			chip.getText().setLine1("%"+chip.getText().getLine1().substring(1));
    			chip.getText().supressUpdate();
    			
    			RedstoneListener redListener = (RedstoneListener) chip.getExtra();
    			redListener.onSignAdded(CraftBook.getWorld(chip.getCBWorld()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    		}
    		else if(!chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '^')
    		{
    			chip.getText().setLine1("^"+chip.getText().getLine1().substring(1));
    			chip.getText().supressUpdate();
    		}
    	}
    }
    
    protected void detectEntity(World world, Vector lever, BlockArea area, ChipState chip)
    {
    	SignText text = UtilIC.getSignTextWithExtension(chip);
    	String[] args = text.getLine3().split("\\+", 2);
        
        DetectEntityInArea detectEntity = new DetectEntityInArea(area, lever, args[0], args.length > 1 ? args[1] : null, null, null);
        etc.getServer().addToServerQueue(detectEntity);
    }
    
    public class DetectEntityInArea implements Runnable
    {
    	private final BlockArea AREA;
    	private final Vector LEVER;
    	private final String ENTITY_NAME;
    	private final String RIDER_NAME;
    	private final WorldLocation DESTINATION;
    	private final String[] MESSAGES;
    	
    	public DetectEntityInArea(BlockArea area, Vector lever, String entityName, String riderName, WorldLocation destination, String[] messages)
    	{
    		AREA = area;
    		LEVER = lever;
    		ENTITY_NAME = entityName;
    		RIDER_NAME = riderName;
    		DESTINATION = destination;
    		MESSAGES = messages;
    	}
    	
		@Override
		public void run()
		{
			boolean output = false;
			
			OWorldServer oworld = CraftBook.getOWorldServer(AREA.getCBWorld());
			for(@SuppressWarnings("rawtypes")
    		Iterator it = oworld.e.iterator(); it.hasNext();)
    		{
    			Object obj = it.next();
    			
    			if(!(obj instanceof OEntity))
    			{
    				//outdated?
    				return;
    			}
    			
    			BaseEntity entity = new BaseEntity((OEntity)obj);
    			
    			if(AREA.containsPoint(AREA.getCBWorld(),
										OMathHelper.c(entity.getX()),
										OMathHelper.c(entity.getY()),
										OMathHelper.c(entity.getZ()) )
    				&& UtilEntity.isValidEntity(entity, ENTITY_NAME)
    				&& (RIDER_NAME == null || RIDER_NAME.isEmpty()
    					|| (UtilEntity.riddenByEntity(entity.getEntity()) != null && UtilEntity.isValidEntity(new BaseEntity(UtilEntity.riddenByEntity(entity.getEntity())), RIDER_NAME)) )
    				)
    			{
    				output = true;
    				
    				if(DESTINATION != null)
    				{
    					if(MESSAGES != null && entity.isPlayer())
    					{
    						Player player = new Player((OEntityPlayerMP)entity.getEntity());
    						for(String message : MESSAGES)
    	                	{
    	                		if(message == null)
    	                			break;
    	                		player.sendMessage(Colors.Gold+message);
    	                	}
    					}
    					
    					if(entity.isPlayer())
    					{
    						Player player = new Player((OEntityPlayerMP)entity.getEntity());
    						//player.teleportTo(DESTINATION);
    						CraftBook.teleportPlayer(player, DESTINATION);
    					}
    					else
    					{
    						//entity.teleportTo(DESTINATION);
    						CraftBook.teleportEntity(entity, DESTINATION);
    					}
    				}
    				
    				break;
    			}
    		}
			
			Redstone.setOutput(AREA.getCBWorld(), LEVER, output);
		}
    }
}
