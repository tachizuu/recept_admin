package nu.te4.beans;

import com.mysql.jdbc.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpSession;
import nu.te4.utilities.ConnectionFactory;

@Named
@RequestScoped
public class AdminBean
{
    private String username;
    private String password;
    
    private String newUsername;
    private String newPassword;
    private int newPermission;
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public void setNewUsername(String username)
    {
        newUsername = username;
    }
    
    public String getNewUsername()
    {
        return newUsername;
    }
    
    public String getNewPassword()
    {
        return newPassword;
    }
    
    public void setNewPassword(String password)
    {
        newPassword = password;
    }
    
    public int getNewPermission()
    {
        return newPermission;
    }
    
    public void setNewPermission(int permission)
    {
        newPermission = permission;
    }
    
    public List<JsonObject> getAccounts()
    {
        try
        {
            Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement("select username, permission_level FROM users");
            ResultSet rs = ps.executeQuery();
            
            List<JsonObject> accounts = new ArrayList<>();
            while(rs.next())
            {
                JsonObject account = Json.createObjectBuilder()
                        .add("username", rs.getString("username"))
                        .add("permission_level", rs.getInt("permission_level"))
                        .build();
                accounts.add(account);
            }
            return accounts;
        }
        catch(Exception e)
        {
            System.out.println("getAccounts Error -> " + e.getMessage());
        }
        
        return null;
    }
    
    public String addAccount()
    {
        String hashed_password = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        try
        {
            Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO users VALUES (?, ?, ?)");
            
            ps.setString(1, newUsername);
            ps.setString(2, hashed_password);
            ps.setInt(3, newPermission);
            
            ps.executeUpdate();
        }
        catch(Exception e)
        {
            System.out.println("addAccount error -> " + e.getMessage());
        }
        
        return "admin";
    }
    
    public String deleteAccount(String username)
    {
        try
        {
            Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE username = ?");
            ps.setString(1, username);
            
            ps.executeUpdate();
        }
        catch(Exception e)
        {
            System.out.println("deleteAccount error -> " + e.getMessage());
        }
        return "admin";
    }
    
    public String validateCredentials()
    {
        boolean valid = validate(username, password);
        if(valid)
        {
            //inloggningen stämmer, skapa session och skicka till admin.xhtml
            HttpSession session = getSession();
            session.setAttribute("username", username);
            return "admin";
        }
        else
        {
            //felaktig inloggning, skicka tillbaka till index.xhtml
            return "index";
        }
        
    }
    
    private boolean validate(String username, String password)
    {
        try
        {
            Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
            ps.setString(1, username);
            
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())
            {
                if(BCrypt.checkpw(password, rs.getString("hashed_password")) && rs.getInt("permission_level") >= 1)
                {
                    //lösenordet stämmer och kontot har admin-rättigheter = returnera true
                    return true;
                }
            }
            
            conn.close();
        }
        catch(Exception e)
        {
            System.out.println("validate error -> " + e.getMessage());
        }
        return false;
    }
    
    public String logout()
    {
        HttpSession session = getSession();
        session.invalidate();
        return "index";
    }
    
    public String getSessionUsername()
    {
        return (String)getSession().getAttribute("username");
    }
    
    public static HttpSession getSession()
    {
        return (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    }
}