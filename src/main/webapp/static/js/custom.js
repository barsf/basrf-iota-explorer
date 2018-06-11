$(function () {
    $('.search').find('input').bind('keypress', function (event) {

        if (event.keyCode == "13") {
            $(this).attr("disabled", "true");
            $.post("/hash/" + this.value, "", function (data) {
                window.location.href = window.location.protocol + "//" + window.location.host + "/" + data;
            });
        }
    });

    setTimeout(function () {
        jQuery('.copy').zclip({
            path: '../../static/js/ZeroClipboard.swf',
            copy: function () {
                return $(this).prev().text();
            },
            beforeCopy: function () {
                $(".copy").css("background-color", "");
            },
            afterCopy: function () {
                $(this).css("background-color", "green");
            }
        })
    }, 400);

    setTimeout(function () {
        if ($("#worth").length > 0) {
            $.get("https://min-api.cryptocompare.com/data/price?fsym=IOT&tsyms=USD", function (data) {
                usd = data.USD;
                var i = amount.innerText.replace(" i", "").replace(/,/g, "");
                $("#worth").text('$' + formatCurrency(usd * i / 1000000));
            });
        }
    }, 1400);
});


var loadAddress = function () {
    var url = window.location.href;
    var cutIndex = url.lastIndexOf("/");
    var hash = url.substr(cutIndex, url.length);
    var detailUrl = url.substr(0, cutIndex) + "/detail" + hash;
    var usedUrl = url.substr(0, cutIndex) + "/getIsUsed/" + hash;
    var banceUrl = url.substr(0, cutIndex) + "/getBalance/" + hash;
    $.postJSON(usedUrl, "", function (data) {
       $("#Used").text(data.used);
       $("#Used").removeClass("shan");
    });
    $.postJSON(banceUrl, "", function (data) {
        if(data.balance){
            $("#balance").text(formatAmount(data.balance));
        }
        $("#balance").removeClass("shan");
    });

    $.postJSON(detailUrl, "", function (data) {
        $("#numberoftran").text(data.numberoftran);
        $("#received").text(formatAmount(data.received));
        $("#sent").text(formatAmount(data.sent));
        $("#balance").text(formatAmount(data.balance));
        $("#signature").text(data.signature);
        if (data.cut) {
            $("#tx_additive").html("<span><i class='fa fa-warning'></i> only show the last 100 transactions</span>");
        }
        if (!data.checkok) {
            $("#tx_additive").html($("#tx_additive").html() + "<span><i class='fa fa-warning'></i> data may unsynchronized!</span>");
        }
        amount = data.value;
        for (var i = 0; i < data.list.length; i++) {
            var map = data.list[i];
            var $newTr = $('#temp_tr').clone();
            $newTr.attr("id", "tr_line" + i);
            var temp = map;
            var tdList = $newTr.find("td");
            var transTime = temp.attachmentTimestamp > temp.timestamp ? temp.attachmentTimestamp : temp.timestamp;
            tdList[0].innerText = formatDate(new Date(parseInt((transTime + "000").substring(0, 13))));
            tdList[1].innerHTML = "<a href=\"/tran/" + temp.hash + "\">" + temp.hash + "</a>";
            tdList[2].innerHTML = "<a href=\"/bundle/" + temp.bundle + "\">" + temp.bundle + "</a>";
            tdList[3].innerHTML = "<span class=\"tx-value\"><span class=\"break\">\n" +
                "                            <span>" + temp.value + "</span> </span>\n" +
                "                        </span>";
            tdList[4].innerHTML = tx_confirm_check(temp.snapshot, "tx-state");
            $newTr.appendTo($('#temp_tr').parent());
        }
        $('#temp_tr').remove();
        $(".shan[id!='Used'][id!='balance']").removeClass("shan");

    }, function (data) {
        if (data.status === 200) {
            if (data.statusText === "parsererror") {
                window.location = "/hash/notfound" + hash;
            }
        }
    });
};

var loadTrans = function () {
    var url = window.location.href;
    var cutIndex = url.lastIndexOf("/");
    var hash = url.substr(cutIndex, url.length);
    var detailUrl = url.substr(0, cutIndex) + "/detail" + hash;

    $.postJSON(detailUrl, "", function (data) {
        $("#address").html("<a href = \"/addr/" + data.address + "\">" + data.address + "</a>");
        $("#amount").text(formatAmount(data.value));
        if (data.attachmentTimestamp != 0) {
            $("#time").text(formatDate(new Date(data.attachmentTimestamp)));
        } else {
            $("#time").text(formatDate(new Date(data.timestamp)));
        }
        $("#status").html(tx_confirm_check(data.snapshot, "tx-state-x"));
        $("#tag").html("<a>" + data.tag + "</a>");
        // $("#worth").text(usd * data.value.innerText / 1000000);
        $("#pow").text("");
        $("#index").text(data.currentIndex + "/" + data.lastIndex);

        if (data.leftTran.length === 81) {
            $("#index_left").attr("href", "/tran/" + data.leftTran);
        } else {
            $("#index_left").css("visibility", "hidden");
        }
        if (data.rightTran.length === 81) {
            $("#index_right").attr("href", "/tran/" + data.rightTran);
        } else {
            $("#index_right").css("visibility", "hidden");
        }
        $("#branch").html("<a href = \"/tran/" + data.branch + "\">" + data.branch + "</a>");
        $("#trunk").html("<a href = \"/tran/" + data.trunk + "\">" + data.trunk + "</a>");
        $("#bundle").html("<a href = \"/bundle/" + data.bundle + "\">" + data.bundle + "</a>");
        $("#signature").text("");
        $(".shan").removeClass("shan");
    }, function (data) {
        if (data.status === 200) {
            if (data.statusText === "parsererror") {
                window.location = "/hash/notfound" + hash;
            }
        }
    });
};


var loadBundle = function () {
        var url = window.location.href;
        var cutIndex = url.lastIndexOf("/");
        var hash = url.substr(cutIndex, url.length);
        var detailUrl = url.substr(0, cutIndex) + "/detail" + hash;

        $.postJSON(detailUrl, "", function (data) {

                $("#amount").text(formatAmount(data.total));
                $("#time").text(formatDate(new Date(data.time)));
                $("#status").html(tx_confirm_check(data.status, "tx-state-x"));


                for (var i = 0; i < data.list.length; i++) {

                    var $newBt = $('#bundle_temp').clone().removeAttr("hidden").removeAttr("id");
                    var $inputHome = $newBt.find(".input_home");
                    var $outputHome = $newBt.find(".output_home");

                    var map = data.list[i];
                    var inputSize = 0;
                    var outputSize = 0;
                    var minTime = 9523918411914;
                    var confirm = 0;
                    for (var ind in map) {
                        var theTrans = map[ind];
                        if (theTrans.currentIndex === 0) {
                            // 约定-1 表示reattach
                            // 约定 -987654321 表示invalided
                            confirm = theTrans.snapshot;
                        }


                        var transTime = theTrans.attachmentTimestamp === 0 ? theTrans.timestamp : theTrans.attachmentTimestamp;
                        minTime = minTime > transTime ? transTime : minTime;
                        if (theTrans.value < 0) {
                            inputSize++;
                            var $newDiv = $('<div>' +
                                '<div class="tx-address"><i class="info fa fa-angle-right"></i> <a class="text-warning break" href="/addr/' + theTrans.address + '">' + theTrans.address + '</a>\n' +
                                '                           </div>\n' +
                                '                        <span class="tx-hash"><a class="text-success break" href = "/tran/' + theTrans.hash + '">' + theTrans.hash + '</a></span>\n' +
                                '                        <span class="tx-value"><span class="break">\n' +
                                '                            <span>' + formatAmount(theTrans.value) + '</span> </span>\n' +
                                '                        </span>' +
                                '' +
                                '</div><br/>');
                            $newDiv.appendTo($inputHome);

                        } else {
                            outputSize++;
                            var $newDiv = $('<div>' +
                                '<div class="tx-address"><i class="info fa fa-angle-right"></i> <a class="text-warning break" href="/addr/' + theTrans.address + '">' + theTrans.address + '</a>\n' +
                                '                           </div>\n' +
                                '                        <span class="tx-hash"><a class="text-success break" href = "/tran/' + theTrans.hash + '">' + theTrans.hash + '</a></span>\n' +
                                '                        <span class="tx-value"><span class="break">\n' +
                                '                            <span>' + formatAmount(theTrans.value) + '</span> </span>\n' +
                                '                        </span>' +
                                '' +
                                '</div><hr/>');
                            $newDiv.appendTo($outputHome);
                        }
                    }
                    $newBt.find(".b-h-l").find("small").text(formatDate(new Date(minTime)));
                    $newBt.find(".b-h-r").find("span").html(tx_confirm_check(confirm, "tx-state-x"));
                    $inputHome.find(".title").text(inputSize + (inputSize > 1 ? " inputs" : " input"));
                    $outputHome.find(".title").text(outputSize + (outputSize > 1 ? " outputs" : " output"));
                    $newBt.appendTo($("#bundles"));
                }
                $(".shan").removeClass("shan");
            }, function
                (data) {
                if (data.status === 200) {
                    if (data.statusText === "parsererror") {
                        window.location = "/hash/notfound" + hash;
                    }
                }
            }
        )
        ;
    }
;

var liveTx = function () {
    if (!!window.EventSource) {
        var source = new EventSource('/sse-live');

        source.addEventListener("live", function (e) {
            var oneTrans = JSON.parse(e.data);
            var $template = $("#tx_live_home");

            var newItem = $('<li class="tx_list"></li>').html('<div class="passby">' +
                '                        <div style="font-family: fantasy" class="text-left liubian">' +
                formatDate(new Date(Number.parseInt(oneTrans.timestamp + "000"))) +
                '                            <span class="tx-value pp"><span></span>' + oneTrans.value + '</span>' +
                '                        </div>' +
                '                        <div>' +
                '                            <div class="tx-address pp"><i class="info fa fa-angle-right"></i>' +
                '                                <a href="/addr/' + oneTrans.address + '" class="text-warning break">' +
                oneTrans.address +
                '</a>' +
                '                            </div>' +
                '                        </div>' +
                '                        <a href="/tran/' + oneTrans.hash + '" class="text-success tx-hash break" ><i class="info"></i>' +
                oneTrans.hash +
                '                        </a>' +
                '                    </div>');
            newItem.attr("hidden", "hidden");
            newItem.queue(function (next) {
                $(this).prependTo($template);
                next();
            }).show("slow");
            while ($(".tx_list").length > 12) {
                var tlistArray = jQuery.makeArray($(".tx_list"));
                tlistArray.reverse();
                $(tlistArray[0]).remove();
            }
        }, false);

        window.onbeforeunload = function (event) {
            source.close();

            console.info("close sse-live.")
        };
    } else {
        alert("Your browser does not support EventSource!");
    }
};

function tx_confirm_check(snapshot, clasz) {
    if (snapshot === -123456789) {
        return "<i  class='" + clasz + "' style=\"background: blue;\">MILESTONE</i>";
    }
    if (snapshot === -987654321) {
        return "<i  class='" + clasz + "' style=\"background: red;\">INVAILDED</i>";
    }

    if ("tx-state" === clasz) {
        var reaConfirm = "REATTCHCFM"
    } else {
        reaConfirm = "REATTACH-CONFIRMED"
    }

    return snapshot > 0 ?
        "<i class='" + clasz + "' style=\"background: green;\">CONFIRMED</i>" :
        snapshot < 0 ?
            "<i class='" + clasz + "' style=\"background: #2db2a7;\">" + reaConfirm + "</i>" :
            "<i class='" + clasz + "' style=\"background: dimgrey;\">PENDING</i>";
}

function showSignature() {
    var url = window.location.href;
    var cutIndex = url.lastIndexOf("/");
    var detailUrl = url.substr(0, cutIndex) + "/detail" + url.substr(cutIndex, url.length) + "?showSignature=true";

    $.postJSON(detailUrl, "", function (data) {
        $("#signature").text(data.signature);
        $("#signature").parent().removeAttr("hidden");
        $("#signature_separator").removeAttr("hidden");
        $("#signature-btn").remove();
    })
}


$.postJSON = function (url, data, callback, eCallback) {
    return jQuery.ajax({
        'type': 'POST',
        'url': url,
        'contentType': 'application/json',
        'data': JSON.stringify(data),
        'dataType': 'json',
        'success': callback,
        'error': eCallback
    });
};
$.postStr = function (url, data, callback, async) {
    return jQuery.ajax({
        'type': 'POST',
        'url': url,
        'contentType': 'application/json',
        'data': data,
        'dataType': 'json',
        'success': callback,
        'async': async
    });
};

function formatDate(now) {
    var year = now.getFullYear();
    var month = ("0" + (now.getMonth() + 1)).slice(-2);
    var date = ("0" + now.getDate()).slice(-2);
    var hour = ("0" + (now.getHours())).slice(-2);
    var minute = ("0" + (now.getMinutes())).slice(-2);
    var second = now.getSeconds();
    return year + "-" + month + "-" + date + "   " + hour + ":" + minute;
}

function formatCurrency(num) {
    num = num.toString().replace(/\$|\,/g, '');
    if (isNaN(num))
        num = "0";
    sign = (num == (num = Math.abs(num)));
    num = Math.floor(num * 100 + 0.50000000001);
    cents = num % 100;
    num = Math.floor(num / 100).toString();
    if (cents < 10)
        cents = "0" + cents;
    for (var i = 0; i < Math.floor((num.length - (1 + i)) / 3); i++)
        num = num.substring(0, num.length - (4 * i + 3)) + ',' +
            num.substring(num.length - (4 * i + 3));
    return (((sign) ? '' : '-') + num + '.' + cents);
}

function formatAmount(amount) {
    return amount.toLocaleString('en-US') + " i";

}