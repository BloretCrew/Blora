# 本地化配置

本地化文件中支持使用 MiniMessage 格式。

## custom.placeholders

你可以在这里面定义自定义的替换符，使用 \<blora:placeholder:{placeholder}> 来使用。    
例如：可以定义一个 "commandPrefix": "\[命令]"，你可以在任意消息中通过 \<blora:placeholder:commandPrefix> 来使用。

## custom.colors

你可以在这里定义自定义的颜色，使用 \<blora:color:{color}> 来使用。  
例如：可以定义一个 "success": "122,44,55"，你可以在任意消息中通过 \<blora:color:success> 来使用。  
支持的颜色格式如下：

- RGB 格式。例："12,44,59", "123, 104, 98"（允许加空格，用于美观）。
- 7 位的十六进制格式，必须保留开头的 # 符号。例："#66ccff"。
- 原版的颜色。例："gold", "blue"。

## kick.login.username_too_short

当用户名不符合要求时的踢出信息。  
支持的替换符：

- \<length>：将会被替换为配置文件中 minUsernameLength 的值

## kick.login.username_too_long

当用户名不符合要求时的踢出信息。  
支持的替换符：

- \<length>：将会被替换为配置文件中 minUsernameLength 的值

## kick.login.username_contains_invalid_characters

当用户名包含不符合配置文件中 usernameRegex 设置的正则时的踢出信息。  
支持的替换符：

- \<regex>：配置文件中设置的用户名正则表达式

## kick.login.same_ip_login_overcount

相同 IP 登录用户超出上限时的踢出提示

## kick.login.same_ip_register_overcount

相同 IP 注册用户超出上限时的踢出提示

## kick.login.same_old_name_online_player

一种罕见的情况：如果一个正版玩家改名后还未进入服务器，本插件还未处理改名的情况。  
这种情况会导致无法在 mojang 服务器上找到正版玩家，但是本插件的数据库还把同名的玩家被认为是正版用户。   
这种情况只有等改名后的正版玩家重新进入一次服务器后本插件调整完数据才能使用这个名字，故先踢出。

## kick.login.same_name_offline_player

正版用户进入服务器后检测有离线玩家用此用户名登录后踢出的提示。  
两种情况：

1. 一名离线用户先加入了服务器，然后另一名使用相同用户名的玩家在这名玩家加入后才买了正版并进入服务器
2. 正版用户名改名改成了相同玩家的用户名，这种情况更复杂

## kick.login.error_profiling

一般不会出现的情况：服务器检测到玩家是正版登录，但是却无法从 mojang 的服务器上获取到正版信息。  
这个情况一般不会出现，是先从 mojang 获取到正版信息才尝试让玩家进行正版登录的，但保险起见添加了此情况的踢出。

## kick.login.swap_premium_username

一般不会出现的情况：两个正版玩家交换了用户名。

## kick.login.online_profile_but_offline_join

当离线用户使用正版用户的用户名进行登录时触发。

## kick.login.too_many_retries

玩家登录是密码输入次数超出重复次数限制被提出的信息。  
支持的替换符：

- \<limit>：最高可重试的次数

## kick.login.exit

玩家在登录或注册时主动选择退出时弹出的提示。

## kick.ingame.not_accept_eula

当玩家没有同意 EULA 被踢出时显示的信息。

## warning.login.password_incorrect

当玩家登录时密码错误弹出的警告。

## warning.register.confirm_not_same

玩家注册时两次密码输入不一致弹出的警告。

## warning.register.password_strategy_failure_no_username

玩家注册时密码中出现自己的用户名的时候出现的警告。

## warning.register.password_strategy_failure_no_duplicated

玩家注册时密码中出现重复出现同一字符弹出的警告。  
支持的替换符：

- \<limit>：最高可重复出现的次数

## warning.register.password_strategy_failure_no_consecutive

玩家注册时密码中出现连续出现顺序字符的情况
支持的替换符：

- \<limit>：最高可排列的字符数

## warning.register.password_strategy_failure_uppercase_included

玩家注册时密码中未同时出现大小写字母出现的警告。

## warning.register.password_strategy_failure_number_included

玩家注册时密码中未同时出现字母和数字出现的警告。

## warning.register.password_strategy_failure_symbol_included

玩家注册时密码中未同时出现字母和符号出现的警告。

## warning.register.password_strategy_failure_number_and_uppercase_included

玩家注册时密码中未同时出现大小写字母和数字出现的警告。

## warning.register.password_strategy_failure_number_and_symbol_included

玩家注册时密码中未同时出现字母、数字和符号出现的警告。

## warning.register.password_strategy_failure_uppercase_and_symbol_included

玩家注册时密码中未同时出现大小写字母和符号出现的警告。

## warning.register.password_strategy_failure_uppercase_and_number_and_symbol_included

玩家注册时密码中未同时出现大小写字母、数字和符号出现的警告。

## warning.register.password_strategy_failure_illegal_characters

玩家注册时密码中出现非法字符出现的警告。

## warning.register.password_strategy_failure_length_not_secure

玩家注册时密码长度不符合要求出现的警告
支持的替换符：

- \<max>：最大长度
- \<min>：最小长度

## warning.register.password_strategy_failure_in_weak_password_dict

玩家注册时密码存在于弱密码字典中时弹出的警告。

