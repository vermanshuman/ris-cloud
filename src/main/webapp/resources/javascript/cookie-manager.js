
function SetCookie (name, value) 
{      
    var coo=new Cookie(name, value, "/", "Monday, 03-Apr-2090 05:00:00 GMT");    
    coo.Save();
}

function GetCookie (name) 
{
    var coo=new Cookie(name);
    return coo.Load();
}