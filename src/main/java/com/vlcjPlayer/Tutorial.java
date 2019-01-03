package com.vlcjPlayer;

import com.vlcjPlayer.common.Const;
import com.vlcjPlayer.common.PlayStatueEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.binding.internal.libvlc_logo_position_e;
import uk.co.caprica.vlcj.binding.internal.libvlc_marquee_position_e;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.List;

public class Tutorial {

    private static Logger logger = LoggerFactory.getLogger(Tutorial.class);

    public final JFrame frame;

//    private static String url = "http://ginocdn.bybzj.com:8091/guochan/20170429/201704090275/1/hls/index.m3u8";
    private static String url = "https://video.kg.com/17863/517324176642678784.mp4";

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

    private Boolean isFullScreen = false;

    private Boolean isMouseChange = false;

    private volatile static Tutorial install;

    /////////////////JPanel组件////////////////////
    private JPanel contentPane;
    private JPanel controlPanel;


    //////////////////按钮组件///////////////////
    private JButton playButton;
    private JButton stopButton;
    private JButton maxWindowButton;
    private JButton videoCurrentTime;
    private JButton videoEndTime;
    private JSlider volumeJSlider;
    private JSlider videoJSlider;

    //////////////键盘鼠标监听器////////////////////
    private Canvas videoSurface;
    private JMenuItem jMenuItemOpenFile;
    private JMenuItem jMenuItemOpenUrl;

    /////////////菜单栏/////////////////////
    private JMenuBar jMenuBar;
    private Timer mouseTime;

    public static void main(String[] args) {
       if(!new NativeDiscovery().discover()){
           logger.error("vlcj库未安装");
           return;
       }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new Tutorial();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public JFrame getFrame() {
        return frame;
    }

    public static Tutorial getInstall(){
        return install;
    }

    public static Tutorial initTemplate() throws Exception {
        if(install != null){
            return install;
        }
        install = new Tutorial();
        return install;
    }

    /**
     * 加载界面布局
     */
    private Tutorial() throws Exception {
        frame = new JFrame("Vlcj Media Player");
        //窗口大小
        frame.setBounds(100, 100, 900, 700);
        //居中
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //设置窗体无边框
        frame.setUndecorated(false);
        frame.setVisible(true);
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        initClassInstantiation();

        JMenu m1 = new JMenu(Const._OPEN);
        JMenu m2 = new JMenu(Const._HELP);
        jMenuBar.add(m1);
        jMenuBar.add(m2);

        //添加子菜单栏
        jMenuItemOpenFile = new JMenuItem(Const._OPEN_FILE,new ImageIcon(getPngUrl("/image/menu.png")));
        jMenuItemOpenUrl = new JMenuItem(Const._OPEN_URL,new ImageIcon(getPngUrl("/image/menu.png")));
        m1.add(jMenuItemOpenFile);
        m1.add(jMenuItemOpenUrl);

        contentPane.setLayout(new BorderLayout());
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        //按钮添加
        new ComponentModule().addButtonModuleToPanel(controlPanel);
        new ComponentModule().addJSlider(controlPanel);
        if(volumeJSlider == null){
            mediaPlayerComponent.getMediaPlayer().setVolume(Const.defaultVolume);
        }else{
            mediaPlayerComponent.getMediaPlayer().setVolume(volumeJSlider.getValue());
        }
        contentPane.add(mediaPlayerComponent);
        //刷新界面布局
        refreshUI();
        //获取鼠标监听器
        getListerenInstall();

        /**
         * 因为videoPlayer是在java进程之外独立运行的 如果长时间保持运行
         * java GC可能会误认为播放器已经无引用 最终可能会被垃圾回收掉 所以 需要一个
         * 强引用来告知GC mediaPlayerComponent是被引用资源
         */
        //添加事件监听器
        ListenerEvent listenerEvent = new ListenerEvent();
        listenerEvent.start();
        //视频进度监听
        MonitorVideoCurrentTime monitorVideoCurrentTime = new MonitorVideoCurrentTime();
        monitorVideoCurrentTime.start();

        DropTargetAdapter kgd = getDropTarget();

        new DropTarget(contentPane, DnDConstants.ACTION_COPY_OR_MOVE,kgd);

        frame.add(contentPane);
        frame.setJMenuBar(jMenuBar);
        setMediaPlayerComponentAttr();
    }

    private void initClassInstantiation() {
        //添加一个JPane
        contentPane = new JPanel();
        //添加一个控制JPanel 存放控制组件
        controlPanel = new JPanel();
        //菜单栏
        jMenuBar = new JMenuBar();
    }

    /**
     * 获取文件拖拽对象
     */
    private DropTargetAdapter getDropTarget(){
        return new DropTargetAdapter(){
            public void drop(DropTargetDropEvent dtde){
                try{
                    Transferable tf=dtde.getTransferable();
                    if(tf.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        List lt=(List)tf.getTransferData(DataFlavor.javaFileListFlavor);
                        if(lt == null || lt.size() == 0){
                            return;
                        }
                        for (Object aLt : lt) {
                            File f = (File) aLt;
                            playURL(f.getAbsolutePath());
                            break;
                        }
                        dtde.dropComplete(true);
                    }
                    else{
                        dtde.rejectDrop();
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
    }

    private void refreshUI(){
        videoJSlider.setPreferredSize(new Dimension(new Double(frame.getWidth()*0.5).intValue(), 20));
    }

    private void getListerenInstall(){
        videoSurface = mediaPlayerComponent.getVideoSurface();
    }

    /**
     * 设置mediaPlayer的基础属性
     */
    private void setMediaPlayerComponentAttr(){
        playURL(url);
        mediaPlayerComponent.getMediaPlayer().enableMarquee(true);
        //添加图标水印
        mediaPlayerComponent.getMediaPlayer().setLogo(Module.fetchLogo(new File(this.getClass().getResource("/image/dog.png").getPath()),libvlc_logo_position_e.top_left,0.8f));
        mediaPlayerComponent.getMediaPlayer().enableLogo(true);

        //禁用鼠标/键盘事件
        mediaPlayerComponent.getMediaPlayer().setEnableKeyInputHandling(false);
        mediaPlayerComponent.getMediaPlayer().setEnableMouseInputHandling(false);
    }

    public void playURL(String url){
        videoJSlider.setValue(0);
        mediaPlayerComponent.getMediaPlayer().playMedia(url);
    }

    public void stopPlay(){
        mediaPlayerComponent.getMediaPlayer().stop();
    }

    /**
     * 关闭窗口
     */
    public void closeWindow() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public static URL getPngUrl(String path){
        return Tutorial.class.getResource(path);
    }

    private class ComponentModule {

        private void addButtonModuleToPanel(JPanel controlPanel){
            playButton = getButton(new ImageIcon(getPngUrl("/image/play1.png")),Const.play,false,false,false,true,null);
            controlPanel.add(playButton);
            stopButton = getButton(new ImageIcon(getPngUrl("/image/stop.png")),Const.stop,false,false,false,true,null);
            controlPanel.add(stopButton);
            maxWindowButton =getButton(new ImageIcon(getPngUrl("/image/max1.png")),Const.MAXWINDOW,false,false,false,true,null);
            controlPanel.add(maxWindowButton);
            JButton volumeBtn = getButton(new ImageIcon(getPngUrl("/image/volume.png")),null,false,false,false,true,null);
            controlPanel.add(volumeBtn);
            contentPane.add(controlPanel, BorderLayout.AFTER_LAST_LINE);
        }

        private JButton getButton(ImageIcon imageIcon, String title, Boolean isOpaque,
                                      Boolean isFilled, Boolean isFocusPainted, Boolean isBorderPainted, Border isBorder){
            if(imageIcon != null){
                imageIcon.setImage(imageIcon.getImage().getScaledInstance(24, 24,
                        Image.SCALE_DEFAULT));
            }
            JButton btn = new JButton(imageIcon);
            btn.setText(title);
            btn.setOpaque(isOpaque);//设置控件是否透明，true为不透明，false为透明
            btn.setContentAreaFilled(isFilled);//设置图片填满按钮所在的区域
            btn.setMargin(new Insets(0, 0, 0, 0));//设置按钮边框和标签文字之间的距离
            btn.setFocusPainted(isFocusPainted);//设置这个按钮是不是获得焦点
            btn.setBorderPainted(isBorderPainted);//设置是否绘制边框
            btn.setBorder(isBorder);//设置边框

            return btn;
        }

        private void addJSlider(JPanel controlPanel){
            volumeJSlider = new JSlider(JSlider.HORIZONTAL,0,100,70);
            volumeJSlider.setPreferredSize(new Dimension(100, 20));
            controlPanel.add(volumeJSlider);

            videoCurrentTime = getButton(null,"00:00",false,false,false,true,null);
            controlPanel.add(videoCurrentTime);

            videoJSlider = new JSlider(JSlider.HORIZONTAL,0,100,0);
            //设置外观长度
            videoJSlider.setPreferredSize(new Dimension(new Double(frame.getWidth()*0.5).intValue(), 20));
            controlPanel.add(videoJSlider);

            videoEndTime = getButton(null,"00:00",false,false,false,true,null);
            controlPanel.add(videoEndTime);
            contentPane.add(controlPanel,BorderLayout.AFTER_LAST_LINE);
        }
    }

    private static class Module {

        private static Logo fetchLogo(File file, libvlc_logo_position_e positionE, float opacity){
            if(file == null || (!file.exists())){
                logger.error("fetch logo image path not cant empty");
                return Logo.logo();
            }
            if(positionE == null){
                positionE = libvlc_logo_position_e.centre;
            }
            return Logo.logo()
                    .file(file)
                    .position(positionE)
                    .opacity(opacity)
                    .enable();
        }


        /**
         *
         * @param marqueeText  播放器上显示的文本
         * @param size         文本大小
         * @param color        文本颜色
         * @param timeOut      显示时长
         * @param position     显示位置
         * @param opacity      透明度
         */
        private static Marquee fetchMarquee(String marqueeText, Integer size, Color color, Integer timeOut, libvlc_marquee_position_e position, float opacity){
            if(marqueeText == null || marqueeText.equals("")){
                logger.error("fetch marquee text not empty");
                return null;
            }
            if(color == null){
                color = Color.WHITE;
            }
            if(timeOut == null){
                timeOut = 0;
            }
            if(position == null){
                position = libvlc_marquee_position_e.bottom;
            }
            if(opacity==0){
                opacity = 1f;
            }
            return Marquee.marquee()
                    .text(marqueeText)
                    .size(size)
                    .colour(color)
                    .timeout(timeOut)
                    .position(position)
                    .opacity(opacity)
                    .enable();
        }
    }

    private void maxWindows(){
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);//最大化窗体
        maxWindowButton.setText(Const.NORMAL);
        refreshUI();
    }

    private void normalWindows(){
        frame.setExtendedState(JFrame.NORMAL);
        maxWindowButton.setText(Const.MAXWINDOW);
        refreshUI();
    }

    public void setFullScreen(Boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    private Long getVideoTime(){
        return mediaPlayerComponent.getMediaPlayer().getLength();
    }

    private Long getCurrentTime(){
        return mediaPlayerComponent.getMediaPlayer().getTime();
    }

    private void setPlayButton(){
        mediaPlayerComponent.getMediaPlayer().pause();
        playButton.setIcon(new ImageIcon(getPngUrl("/image/play.png")));
        playButton.setText(Const.play);
    }

    private void setPauseButton(){
        mediaPlayerComponent.getMediaPlayer().play();
        playButton.setIcon(new ImageIcon(getPngUrl("/image/oopic_pause.png")));
        playButton.setText(Const.pause);
    }
    private class ListenerEvent extends Thread{

        @Override
        public void run() {
            //监听窗口关闭事件
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("子窗口被关闭"+e.getID());
                    super.windowClosing(e);
                }
            });

            //监听按钮 播放 暂停
            playButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String buttonText = playButton.getText();
                    if(Const.play.equals(buttonText)){
                       setPauseButton();
                       stopButton.setEnabled(true);
                    }else if(Const.pause.equals(buttonText)){
                        setPlayButton();
                    }
                }
            });

            //窗口最大最小监控
            frame.addWindowStateListener(new WindowStateListener() {
                @Override
                public void windowStateChanged(WindowEvent e) {
                    if(6 == e.getNewState()){
                        maxWindows();
                        setFullScreen(true);
                    }else if(0 == e.getNewState()){
                        normalWindows();
                        setFullScreen(false);
                    }
                    refreshUI();
                }
            });

/*            marqueeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediaPlayerComponent.getMediaPlayer().setMarquee(Module.fetchMarquee("marquee text",40,Color.RED,1000,libvlc_marquee_position_e.bottom,0.8f));
                }
            });*/

            //监听按钮 停止
            stopButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediaPlayerComponent.getMediaPlayer().stop();
                    playButton.setIcon(new ImageIcon(getPngUrl("/image/play1.png")));
                    playButton.setText(Const.play);
                    stopButton.setEnabled(false);
                }
            });
            //窗体最大化
            maxWindowButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(Const.MAXWINDOW.equals(maxWindowButton.getText())){
                        maxWindows();
                        setFullScreen(true);
                    }else if(Const.NORMAL.equals(maxWindowButton.getText())){
                        normalWindows();
                        setFullScreen(false);
                    }
                }
            });
            //播放器状态监听
            mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter(){
                @Override
                public void playing(MediaPlayer mediaPlayer) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            frame.setTitle(String.format(
                                    "Media Player - %s",
                                    mediaPlayerComponent.getMediaPlayer().getMediaMeta().getTitle()
                            ));
                            //获取总时长
                            logger.debug("total time:"+mediaPlayerComponent.getMediaPlayer().getLength());
                            videoEndTime.setText(formatTime(mediaPlayerComponent.getMediaPlayer().getLength()));
                            videoJSlider.setMaximum(getVideoTime().intValue());
                            videoJSlider.setValue(getCurrentTime().intValue());
                            setPauseButton();
                        }
                    });
                }

                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    logger.info("播放完成");
                    setPlayButton();
                }

                @Override
                public void error(MediaPlayer mediaPlayer) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Failed to play media",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                            closeWindow();
                        }
                    });
                }

                @Override
                public void stopped(MediaPlayer mediaPlayer) {
                     logger.debug("停止中");
                }

                @Override
                public void paused(MediaPlayer mediaPlayer) {
                    logger.debug("暂停中");
                }
            });

            //鼠标点击事件
            videoSurface.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    isMouseChange = true;
                    int i = e.getButton();
                    if (i == MouseEvent.BUTTON1) {
                        if(e.getClickCount() == 1){
                            mouseTime = new Timer(400, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (playButton.getText().equals(Const.play)) {
                                        setPauseButton();
                                    } else {
                                        setPlayButton();
                                    }
                                    mouseTime.stop();
                                }
                            });
                            mouseTime.restart();
                        } else if (e.getClickCount() == 2 && mouseTime.isRunning()) {
                            mouseTime.stop();
                            if(isFullScreen){
                                normalWindows();
                                setFullScreen(false);
                            }else{
                                maxWindows();
                                setFullScreen(true);
                            }
                        }
                    }
                }
            });

            //鼠标移动事件
            videoSurface.addMouseMotionListener(new MouseMotionListener() {

                /**
                 * 鼠标左键点击状态移动监控
                 */
                @Override
                public void mouseDragged(MouseEvent e) {
                    isMouseChange = true;
                    logger.debug("鼠标移动事件1:"+ e.getX()+" : "+e.getY()+ "mouseChane:"+isMouseChange);
                }

                /**
                 * 普通监控
                 */
                @Override
                public void mouseMoved(MouseEvent e) {
                    logger.debug("鼠标移动事件2:"+ e.getX()+" : "+e.getY() + "mouseChane:"+isMouseChange);
                    isMouseChange = true;
                }
            });
            //键盘按下事件
            videoSurface.addKeyListener(new KeyListener() {

                @Override
                public void keyTyped(KeyEvent e) {
                    if(e.getKeyChar()=='p'){
                        normalWindows();
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    logger.debug("click key2:"+e.getKeyCode()+" : "+e.getKeyChar());
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    logger.debug("click key3:"+e.getKeyCode()+" : "+e.getKeyChar());
                }
            });

            //音量滑动条监控
            volumeJSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider) e.getSource();
                    mediaPlayerComponent.getMediaPlayer().setVolume(source.getValue());
                }
            });
            //视频滑动条监控
            videoJSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    logger.debug("滑条值:"+source.getValue()+"  max:"+source.getMaximum()+ "isValid:"+source.isValid());
                    if(isMouseChange){
                        mediaPlayerComponent.getMediaPlayer().setTime(source.getValue());
                    }
                }
            });

            //菜单栏点击事件  (打开网络文件)
            jMenuItemOpenUrl.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String inputInfo = JOptionPane.showInputDialog("请输入视频链接:");
                    inputInfo = inputInfo.replaceAll(" ","");
                    if(inputInfo!=null && !inputInfo.equals(""))
                    playURL(inputInfo);
                }
            });

            //菜单栏点击事件  (打开本地文件)
            jMenuItemOpenFile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser jfc=new JFileChooser();
                    jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
                    jfc.showDialog(new JLabel(), "选择");
                    File file=jfc.getSelectedFile();
                    if(file.isDirectory()){
                        JOptionPane.showMessageDialog(null, "请选择有效的视频格式文件", "错误", JOptionPane.ERROR_MESSAGE);
                    }else if(file.isFile()){
                        playURL(jfc.getSelectedFile().getName());
                    }
                }
            });
        }

    }

    /**
     * 监听视频进度
     */
    class MonitorVideoCurrentTime extends Thread{

        @Override
        public void run() {
            String playStatus;
            while (true){
                try {
                    playStatus = mediaPlayerComponent.getMediaPlayer().getMediaState().name();
                    logger.debug("status:"+playStatus);
                    frame.setTitle(mediaPlayerComponent.getMediaPlayer().getMediaMeta().getTitle()+"    状态:"+ PlayStatueEnum.getEnumStatus(playStatus).getDescribe());
                    isMouseChange = false;
                    if(getVideoTime() == 0L){
                        continue;
                    }
                    Long currentTime = mediaPlayerComponent.getMediaPlayer().getTime();
                    videoCurrentTime.setText(formatTime(currentTime));
                    logger.debug("max value:"+videoJSlider.getMaximum()+" 当前值:"+currentTime);
                    videoJSlider.setValue(getCurrentTime().intValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String formatTime(Long totalTime) {
        int time = Integer.parseInt(totalTime / 1000+"");
        Integer elapsedHours = Integer.parseInt(time / (60 * 60)+"");
        if (elapsedHours > 0) {
            time -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = time / 60;
        int elapsedSeconds = time - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (elapsedHours > 0) {
            return String.format("%d:%02d:%02d",
                    elapsedHours, elapsedMinutes, elapsedSeconds);
        } else {
            return String.format("%02d:%02d",
                    elapsedMinutes, elapsedSeconds);
        }
    }
}
