import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Hylun(https://gitee.com/hylun)
 * 2018/1/3
 */
public class View extends JFrame {

    public static void main(String[] args) {
        String ANDROID_HOME = System.getenv("ANDROID_HOME");
        if(ANDROID_HOME==null) ANDROID_HOME = new File("").getAbsolutePath();
        Jump.adb = ANDROID_HOME+"/platform-tools/adb";
        String error = "请避免手机被360或QQ连接";
        try {
            Jump.exec(Jump.adb + " start-server");
        }catch (Exception e){
            error = "无法连接手机，请打开USB调试模式";
        }
        View tt = new View(error);
        tt.setVisible(true);
    }

    private View(String str) {
        super("跳一跳机器人");
        setSize(220, 220);
        setResizable(false);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER,0,10);
        getContentPane().setLayout(layout);

        getContentPane().add(new JLabel("请打开手机USB调试模式"));
        getContentPane().add(new JLabel("然后打开微信跳一跳游戏"));

        getContentPane().add(new JLabel("请调节时间系数"));
        JTextField textField = new JTextField(3);
        textField.setText(""+Jump.rate);
        getContentPane().add(textField);

        JCheckBox checkBox = new JCheckBox("自动重新开始游戏");
        checkBox.setSelected(true);
        checkBox.addActionListener(e->{
            Jump.restart = checkBox.isSelected();
        });
        getContentPane().add(checkBox);

        JLabel error = new JLabel(str);
        error.setForeground(Color.red);
        getContentPane().add(error);

        String s1 = "开始游戏",s2="停止运行";
        JButton button = new JButton(s1);
        button.addActionListener(ae -> {
            if(!Jump.run){
                try {
                    float f = Float.parseFloat(textField.getText());
                    if(f<1 || f>3) throw new Exception("");
                    Jump.rate = f;
                }catch (Exception e){
                    textField.setText(""+Jump.rate);
                    error.setText("时间系数请设置为1-3之间的数字");
                }
                new Thread(() -> {
                    try {
                        Jump.run = true;
                        button.setText(s2);
                        error.setText("=====正在运行中=====");
                        Jump.start();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        error.setText("系统错误，已停止运行");
                        Jump.run = false;
                        button.setText(s1);
                    }
                }).start();
            }else{
                button.setText(s1);
                error.setText("=====已停止运行=====");
                Jump.run = false;
            }
        });
        getContentPane().add(button);
    }

}
