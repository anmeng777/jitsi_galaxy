/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.customcontrols;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.service.keybindings.*;
import java.util.*;

public abstract class SIPCommFrame
    extends JFrame
    implements Observer
{
    private Logger logger = Logger.getLogger(SIPCommFrame.class);

    ActionMap amap;
    InputMap imap;
    KeybindingSet bindings = null;

    public SIPCommFrame()
    {
        this.setIconImage(
            ImageLoader.getImage(ImageLoader.SIP_COMMUNICATOR_LOGO));

        // In order to have the same icon when using option panes
        JOptionPane.getRootFrame().setIconImage(
                ImageLoader.getImage(ImageLoader.SIP_COMMUNICATOR_LOGO));

        this.addWindowListener(new FrameWindowAdapter());

        amap = this.getRootPane().getActionMap();

        amap.put("close", new CloseAction());

        imap = this.getRootPane().getInputMap(
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * The action invoked when user presses Escape key.
     */
    private class CloseAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            saveSizeAndLocation();
            close(true);
        }
    }
    
    /**
     * Sets the input map to utilize a given category of keybindings. The frame
     * is updated to reflect the new bindings when they change. This replaces
     * any previous bindings that have been added.
     * @param category set of keybindings to be utilized
     */
    protected void setKeybindingInput(KeybindingSet.Category category)
    {
        // Removes old binding set
        if (this.bindings != null)
        {
            this.bindings.deleteObserver(this);
            resetInputMap();
        }

        // Adds new bindings to input map
        KeybindingsService service = GuiActivator.getKeybindingsService();
        this.bindings = service.getBindings(category);

        for (KeyStroke key : this.bindings.getBindings().keySet())
        {
            String action = this.bindings.getBindings().get(key);
            imap.put(key, action);
        }

        this.bindings.addObserver(this);

    }
    
    /**
    * Bindings the string representation for a keybinding to the action that
    * will be executed.
    * @param binding string representation of action used by input map
    * @param action the action which will be executed when user presses the
    *            given key combination
    */
    protected void addKeybindingAction(String binding, Action action)
    {
        amap.put(binding, action);
    }

    /**
     * Before closing the application window saves the current size and position
     * through the <tt>ConfigurationService</tt>.
     */
    public class FrameWindowAdapter extends WindowAdapter
    {
        public void windowClosing(WindowEvent e)
        {
            saveSizeAndLocation();

            close(false);
        }
    }

    /**
     * Saves the size and the location of this frame through the
     * <tt>ConfigurationService</tt>.
     */
    private void saveSizeAndLocation()
    {
        ConfigurationService configService
            = GuiActivator.getConfigurationService();

        String className = this.getClass().getName();

        try
        {
            configService.setProperty(
                className + ".width",
                new Integer(getWidth()));

            configService.setProperty(
                className + ".height",
                new Integer(getHeight()));

            configService.setProperty(
                className + ".x",
                new Integer(getX()));

            configService.setProperty(
                className + ".y",
                new Integer(getY()));
        }
        catch (PropertyVetoException e1)
        {
            logger.error("The proposed property change "
                    + "represents an unacceptable value");
        }
    }

    /**
     * Sets window size and position.
     */
    public void setSizeAndLocation()
    {
        ConfigurationService configService
            = GuiActivator.getConfigurationService();

        String className = this.getClass().getName();

        String widthString = configService.getString(
            className + ".width");

        String heightString = configService.getString(
            className + ".height");

        String xString = configService.getString(
            className + ".x");

        String yString = configService.getString(
            className + ".y");

        int width = 0;
        int height = 0;

        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

        if(widthString != null && heightString != null)
        {
            width = new Integer(widthString).intValue();
            height = new Integer(heightString).intValue();

            if(width > 0 && height > 0
                && width <= screenWidth && height <= screenHeight)
                this.setSize(width, height);
        }

        int x = 0;
        int y = 0;

        if(xString != null && yString != null)
        {
            x = new Integer(xString).intValue();
            y = new Integer(yString).intValue();

            if (x >= 0 && y >= 0)
                this.setLocation(x, y);
            else
                this.setCenterLocation();
        }
        else
        {
            this.setCenterLocation();
        }
    }
    
    /**
     * Positions this window in the center of the screen.
     */
    private void setCenterLocation()
    {
        this.setLocation(
                Toolkit.getDefaultToolkit().getScreenSize().width/2
                    - this.getWidth()/2,
                Toolkit.getDefaultToolkit().getScreenSize().height/2
                    - this.getHeight()/2
                );
    }

    /**
     * Checks whether the current component will 
     * exceeds the screen size and if it do will set a default size 
     */
    private void ensureOnScreenLocationAndSize()
    {
        int x = this.getX();
        int y = this.getY();
        
        int width = this.getWidth();
        int height = this.getHeight();
        

        if(x < 0 || y < 0)
        {
            this.setLocation(
                Toolkit.getDefaultToolkit().getScreenSize().width/2
                    - this.getWidth()/2,
                50
            );
            
            x = this.getX();
            y = this.getY();
        }

        Rectangle virtualBounds = ScreenInformation.getScreenBounds();

        // in case any of the sizes exceeds the screen size
        // we set default one
        if (!(virtualBounds.contains(x, y) && virtualBounds.contains(x + width,
                y + height)))
        {

            if (x + width > virtualBounds.width)
            {
                // location of window is too far to the right
                x = virtualBounds.width - width;
                if (x < 20)
                {
                    x = 20;
                    width = virtualBounds.width - 40;
                }
            }
            if (y + height > virtualBounds.height)
            {
                // location of window is too far to the right
                y = virtualBounds.height - height;
                if (y < 20)
                {
                    y = 20;
                    height = virtualBounds.height - 40;
                }
            }
            this.setPreferredSize(new Dimension(width, height));
            this.setSize(width, height);
            this.setLocation(x, y);

        }
    }
    
    /**
     * Overwrites the setVisible method in order to set the size and the
     * position of this window before showing it.
     */
    public void setVisible(boolean isVisible)
    {
        if (isVisible)
        {
            this.pack();
            this.setSizeAndLocation();

            this.ensureOnScreenLocationAndSize();
        }

        super.setVisible(isVisible);
    }
    
    /**
     * Overwrites the dispose method in order to save the size and the position
     * of this window before closing it.
     */
    public void dispose()
    {
        this.saveSizeAndLocation();
        
        super.dispose();
    }

    private void resetInputMap()
    {
        imap.clear();
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    }

    // Listens for changes in binding sets so they can be reflected in the input
    // map
    public void update(Observable obs, Object arg)
    {
        if (obs instanceof KeybindingSet)
        {
            KeybindingSet changedBindings = (KeybindingSet) obs;
            resetInputMap();
            for (KeyStroke binding : changedBindings.getBindings().keySet())
            {
                String action = changedBindings.getBindings().get(binding);
                imap.put(binding, action);
            }
        }
    }

    /**
     * All functions implemented in this method will be invoked when user
     * presses the Escape key. 
     */
    protected abstract void close(boolean isEscaped);
}
