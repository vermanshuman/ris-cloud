var Cookie = function(name, value, path, expires, domain, secure)
{
	this.Name = name;
	this.Value = value;
	this.Path = path;
	this.Expires = expires;
	this.Domain = domain;
	this.Secure = secure;
};

Cookie.prototype.MaxSize = 4000; //size in KB

Cookie.prototype.toString = function()
{
	return this.Value;
};

Cookie.prototype.Append = function(newValue)
{
	this.Value += newValue;
};

Cookie.prototype.$GetValue = function( startIndex )
{  
	var endIndex = document.cookie.indexOf( ";", startIndex );  
	if( endIndex == -1 )    
		endIndex = document.cookie.length;
	var cookieValue = document.cookie.substring(startIndex, endIndex);
	if( cookieValue == "" )
		return null;
	else
		return unescape( cookieValue );
};

Cookie.prototype.Load = function()
{  
	var arg = this.Name + "=";  
	var alen = arg.length;  
	var clen = document.cookie.length;  
	
	var i = 0;  
	while( i < clen )
	{    
		var j = i + alen;    
		if( document.cookie.substring(i, j) == arg )
		{
			this.Value = this.$GetValue(j);
			return this.Value;
		}
		i = document.cookie.indexOf( " ", i ) + 1;    
		if( i == 0 ) break;   
	}  
	
	return null;
};

Cookie.prototype.Save = function()
{
	var newCookie = this.Name + "=" + escape(this.Value) +
	((this.Expires == null) ? "" : ("; expires=" + this.Expires)) +
	((this.Path == null) ? "" : ("; path=" + this.Path)) +
	((this.Domain == null) ? "" : ("; domain=" + this.Domain)) +
	((this.Secure == true) ? "; secure" : "");
	
	if( newCookie.length > Cookie.MaxSize )
		throw Error("Cookie length was " + newCookie.length + "kb but cookies cannot exceed " + Cookie.MaxSize + "kb");
	
	document.cookie = newCookie;
};

Cookie.prototype.Delete = function()
{  
	var exp = new Date();  
	exp.setTime(exp.getTime() - 1);
	document.cookie = this.Name + "=null;expires=" + exp.toGMTString();
};
