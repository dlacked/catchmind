package SwingTest;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import SwingTest.MyPoint;
import SwingTest.EditorEx.DrawingPanel;
import SwingTest.EditorEx.ServerThread;

import java.awt.Color;

public class ClientEx extends JFrame {

	private Color currentColor = Color.BLACK; 
	private DrawingPanel drawPane = null;
	private int penWidth = 3;
    private String answer = "";
    private BufferedReader in = null;
	private BufferedWriter out = null;
	private Socket socket = null;
	private DefaultListModel<String> model = new DefaultListModel<>();
	private JScrollPane listScrollPane;
	private JTextArea consolePane;
	
    public ClientEx() {
    	
    	setTitle("Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //layout
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());

        drawPane = new DrawingPanel();
        
        //설레는 댓글창
        this.consolePane = new JTextArea();
        JScrollPane consoleScrollPane = new JScrollPane(this.consolePane);
        consoleScrollPane.setPreferredSize(new Dimension(200, 100));
        consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        c.add(consoleScrollPane, BorderLayout.SOUTH); 
        
        model = new DefaultListModel<>();
        
		JList<String> list = new JList<>(model);
		list.setEnabled(false);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setFixedCellWidth(150);  
		model.addElement("***************************************");
		model.addElement("Sys: 정답을 입력해주세요.");
		model.addElement("***************************************");
		
		this.listScrollPane = new JScrollPane(list);
		this.listScrollPane.setPreferredSize(new Dimension(200, 100));
        this.listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		c.add(this.listScrollPane, BorderLayout.EAST);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(listScrollPane, BorderLayout.CENTER);
		rightPanel.add(consoleScrollPane, BorderLayout.SOUTH);

        drawPane.setBackground(Color.WHITE);
		c.add(rightPanel, BorderLayout.EAST);
        c.add(drawPane, BorderLayout.CENTER);
        
		pack(); 
		setSize(1024, 712);
        setVisible(true);
        

        new ClientThread().start();
        
    }

    class ClientThread extends Thread {
    	 @Override
         public void run() {
             try {
                 socket = new Socket("localhost", 9999);
                 SwingUtilities.invokeLater(() -> model.addElement("Sys: Server와 연결되었습니다."));

                 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 
                 new ReceiveThread(in).start();
                 
                 InputMap im = consolePane.getInputMap(JComponent.WHEN_FOCUSED);
                 ActionMap am = consolePane.getActionMap();
                 
   
                 im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");
                 am.put("enterPressed", new AbstractAction() {
                   @Override
                   public void actionPerformed(ActionEvent e) {
       
                       String text = consolePane.getText();
                       
                       try {
                           // 1. GUI에 메시지 등록 (Enter 키를 누르면 바로 등록되어야 함)
                    	   model.addElement("Client: " + text);
                    	   consolePane.setText(""); // 입력창 초기화
                           
                           if (out != null) {
                        	   if (text.equals(answer)) {
	                           		model.addElement("***************************************");
	                        		model.addElement("Sys: Client가 정답을 맞췄습니다!");
	                        		model.addElement("***************************************");
                        	   }
                        	   out.write(text + "\n"); // 서버가 readLine() 하도록 \n 추가
                        	   out.flush();
                           }
                           
                           // 3. 스크롤 조정 (안정화)
                           SwingUtilities.invokeLater(() -> {
                               JScrollBar bar = listScrollPane.getVerticalScrollBar();
                               bar.setValue(bar.getMaximum());
                           });
       	                    
                       } catch (IOException ele) {
                       }
                   }
               });
             } catch (IOException e) {
             }
         }
    }
    
    class ReceiveThread extends Thread {
    	BufferedReader in;
    	
    	ReceiveThread(BufferedReader in) {
    		this.in = in;
    	}
    	
    	@Override
    	public void run() {
    		String receivedAnswer;
    		try {
    			while(true) {
    				receivedAnswer = in.readLine();
                    if (receivedAnswer == null) break; 
                    ClientEx.this.answer = receivedAnswer;
    			}
    			// GUI 업데이트는 반드시 EDT에서
                
    		} catch (IOException e) {
    			
    		}
    	}
    }
    

    //개중요
    class DrawingPanel extends JPanel{
   	
//    	private Vector<Vector<MyPoint>> splineList;
//    	private Vector<Vector<MyPoint>> erasedSplineList;
//    	private Vector<MyPoint> currentSpline = null;

    }	
    
    public static void main(String[] args) {
        new ClientEx();
    }
}