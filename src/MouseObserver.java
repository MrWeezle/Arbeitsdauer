import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


/**
 * This class checks the position every #DELAY milliseconds and 
 * informs all registered MouseMotionListeners about position updates.
 */
public class MouseObserver implements Runnable {
    /* the resolution of the mouse motion */
    private static final int DELAY = 10;

    private static Component component;
    private static Date currentTime2;
    private Timer timer;
    private Set<MouseMotionListener> mouseMotionListeners;
    private static int newDay = 1, mouseMoved = 0,curHour,point_x,point_y;//movedInWork;
    private static String oldDate= "14.06.2015", newDate;
    private static final long HOURS = (long) (7*3600*1000);

    static JDialog main = new JDialog();
    static JPanel jpMenu = new JPanel();
    static JLabel label = new JLabel(),jlClose = new JLabel();
    
    @SuppressWarnings("static-access")
	protected MouseObserver(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("Null component not allowed.");
        }
        this.component = component;
        /* poll mouse coordinates at the given rate */
        timer = new Timer(DELAY, new ActionListener() {
                private Point lastPoint = MouseInfo.getPointerInfo().getLocation();
                /* called every DELAY milliseconds to fetch the
                 * current mouse coordinates */
                public synchronized void actionPerformed(ActionEvent e) {
                    try {
                    	Point point = MouseInfo.getPointerInfo().getLocation();
                        if (!point.equals(lastPoint)) {
                        	fireMouseMotionEvent(point);
                        }
                        lastPoint = point;
                    }catch(Exception e1) {
                    	e1.printStackTrace();
                    }
                }
            });
        mouseMotionListeners = new HashSet<MouseMotionListener>();
    }
    public Component getComponent() {
        return component;
    }
    public void start() {
        timer.start();
    }
    public void stop() {
        timer.stop();
    }
    public void addMouseMotionListener(MouseMotionListener listener) {
        synchronized (mouseMotionListeners) {
            mouseMotionListeners.add(listener);
        }
    }
    public void removeMouseMotionListener(MouseMotionListener listener) {
        synchronized (mouseMotionListeners) {
            mouseMotionListeners.remove(listener);
        }
    }
    protected void fireMouseMotionEvent(Point point) {
        synchronized (mouseMotionListeners) {
            for (final MouseMotionListener listener : mouseMotionListeners) {
                final MouseEvent event = new MouseEvent(component, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(),0, point.x, point.y, 0, false);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        listener.mouseMoved(event);
                    }
                });
            }
        }
    }
    /* Testing the ovserver */
    @SuppressWarnings("static-access")
	public static void main(String[] args) {
        main.setSize(200,60);
//        main.setLocation(-200, 965);
        main.setLocation(0,0);
        main.setUndecorated(true);
        main.setAlwaysOnTop(true);
        main.setDefaultCloseOperation(main.HIDE_ON_CLOSE);
        
        jpMenu = new JPanel();
		jpMenu.setSize(main.getWidth(), 18);
		jpMenu.setBackground(Color.decode("#1F497D"));
		jpMenu.setLayout(null);
	    jpMenu.setLocation(0, 0);	    
	    jpMenu.addMouseListener(new MouseAdapter() {
	        public void mousePressed(MouseEvent e) {
	        	if (!e.isMetaDown()) {
	        		point_x = e.getX();
	        		point_y = e.getY();
	        	}
	        }
	    });

	    jpMenu.addMouseMotionListener(new MouseMotionAdapter() {
	    	public void mouseDragged(MouseEvent e) {
	    		if (!e.isMetaDown()) {
	    			Point p = main.getLocation();
	    			main.setLocation(p.x + e.getX() - point_x, p.y + e.getY() - point_y);
	    			System.out.println("X: "+main.getX()+"\tY: "+main.getY());
	    		}
	    	}
	    });       
        
        final ImageIcon iiClose = new ImageIcon(ClassLoader.getSystemResource("Resource/aqua_remove.gif"));
	    final ImageIcon iiClose2 = new ImageIcon(ClassLoader.getSystemResource("Resource/aqua_remove_w.gif"));
	    jlClose = new JLabel("");
	    jlClose.setSize(16, 16);
	    jlClose.setIcon(iiClose);
	    jlClose.setLocation(jpMenu.getWidth()-18, 1);
	    jlClose.addMouseListener(new MouseAdapter() {
	    	public void mouseEntered(MouseEvent e) {
	    		Cursor c = new Cursor(12);
	    		jlClose.setCursor(c);
	    		jlClose.setIcon(iiClose2);
	    	}
	    	public void mouseExited(MouseEvent e) {
	    		Cursor c = new Cursor(0);
	    		jlClose.setCursor(c);
	    		jlClose.setIcon(iiClose);
	    	}
	    	public void mouseClicked(MouseEvent e) {
	    		System.exit(0);
	    	}
	    });
	    jpMenu.add(jlClose);
	    main.add(jpMenu);
	    main.add(label);
	    
        main.setVisible(true);
        MouseObserver mo = new MouseObserver(main);
        mo.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {
            	if (newDay == 1 && mouseMoved == 0) {
            		mouseMoved = 1;
            		newDay = 0;
            		SimpleDateFormat formatter = new SimpleDateFormat ("dd.MM.yyyy 'um' HH:mm 'Uhr' ");
            		Date currentTime = new Date();
            		Calendar cal = Calendar.getInstance();
            		
            		int test = cal.get(Calendar.HOUR_OF_DAY)* 100 + cal.get(Calendar.MINUTE);
            		long pause = 1800*1000;
            		if (test < 915) {
            			pause = pause + 900*1000;
            		}
            		Date plannedTime = new Date(currentTime.getTime() + (HOURS+pause));
            		label.setText("<html><body><br>Beginn: " + formatter.format(currentTime)+"<br>Ende: "+formatter.format(plannedTime)+"</body></html>");
            		//label.setText("Beginn: " + formatter.format(currentTime)+"\r\rEnde: "+formatter.format(plannedTime));
            	}
            	else {
            		System.out.println("Test");
            		SimpleDateFormat formatter1 = new SimpleDateFormat ("dd.MM.yyyy HH:mm");
            		currentTime2 = new Date();
            		String datetime[] = formatter1.format(currentTime2).split(" ");
            		String tmpTime[] = datetime[1].split(":");
            		curHour = Integer.parseInt(tmpTime[0]);
//            		movedInWork = 1;
            	}
//                System.out.println("mouse moved: " + e.getPoint());
            }
            public void mouseDragged(MouseEvent e) {
//                System.out.println("mouse dragged: " + e.getPoint());
            }
        });
        mo.start();
        Thread t1 = new Thread( new MouseObserver(component) );
        t1.run();
    }
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			SimpleDateFormat formatter = new SimpleDateFormat ("dd.MM.yyyy");
    		Date currentTime = new Date();
    		newDate = formatter.format(currentTime);
//    		System.out.println(newDate);
			if (oldDate.equals(""))
				oldDate = newDate;			
			if (!(oldDate.equals(newDate))) {
				oldDate = newDate;
				newDay = 1;
				mouseMoved = 0;
			}
			if (curHour >=17) {
				
			}
		}		
	}
}