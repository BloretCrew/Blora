# 本地化配置

本地化文件中支持使用 MiniMessage 格式。

服务端版本支持 PAPI，使用 \<papi:{placeholder}> 来使用。  
例如：如果想使用 %player_displayName%，请使用：\<papi:player_displayName>。

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

## mail.sender.unknown

邮件中收到未知发送者时显示的名称。

## command.error.player_only

当一个只能由玩家执行的命令被非玩家执行者执行时的错误。

## command.error.block_only

当一个只能由方块执行的命令被非方块执行者执行时的错误。

## command.error.no_suitable_executor

当一个命令找不到合适的执行器时出现的错误。

## command.blora.modules.message

运行 `/blora modules` 时开头显示的信息。

## command.blora.modules.module

运行 `/blora modules` 时模块行的信息。

## command.blora.modules.module.tooltip

运行 `/blora modules` 时模块行的注释。

## command.blora.modules.module.status.enabled

运行 `/blora modules` 时，模块状态为开启时 <module_status> 的替换内容。

## command.blora.modules.module.status.disabled

运行 `/blora modules` 时，模块状态为关闭时 <module_status> 的替换内容。