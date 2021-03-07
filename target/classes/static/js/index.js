$(function() {
	var monitorChart = undefined;
	var socket= undefined;
	var option = {
		title: {
			text: 'System usage'
		},
		tooltip : {
			trigger: 'axis',
			axisPointer: {
				type: 'cross',
				label: {
					backgroundColor: '#6a7985'
				}
			}
		},
		legend: {
			data:['cpu%','mem%']
		},
		toolbox: {
			feature: {
				/*saveAsImage: {}*/
			}
		},
		grid: {
			left: '3%',
			right: '4%',
			bottom: '3%',
			containLabel: true
		},
		xAxis : [
			{
				type : 'time',
				boundaryGap : false,
			}
		],
		yAxis : [
			{
				type : 'value',
				// max:100,
				name: '',

			}

		],
		// series:[],
		series : [
			{
				name:'cpu%',
				type:'line',
				symbol: 'none',
				data: []

			},
			{
				name:'mem%',
				type:'line',
				symbol: 'none',
				data: []
			}
		],
		color:['#00A65A', '#c23632', '#367FA9']
	};
	var arrayIp = [];
	freshChart();
	/**
	 * fresh Chart
	 *
	 */
	function freshChart() {
		monitorChart = echarts.init(document.getElementById('monitorChart'));
		monitorChart.setOption(option);
		monitorChartRefresh();

	}


	function monitorChartRefresh(newUrl) {
		if (typeof (WebSocket) == "undefined") {
			console.log("not support WebSocket");
		} else {
			console.log("support WebSocket");

			var remoteUrl = "http://" + window.location.host;
			if(newUrl !== undefined) {
				remoteUrl = newUrl
			}
			var wsUrl = remoteUrl.replace("http","ws") + "/ws/monitor"

			socket = new WebSocket(wsUrl);
			//open
			socket.onopen = function () {
				console.log("Socket opened");
				socket.send("send message start(From Client)");
			};
			//receive message
			socket.onmessage = function (msg) {

				if (msg.data.indexOf("monitorCPU") > 0) {
					var result = JSON.parse(msg.data);
					while (option.series[0].data.length >= 300) {
						option.series[0].data.shift();
						option.series[1].data.shift();
					}
					option.series[0].data = option.series[0].data.concat(result.monitorCPU);
					option.series[1].data = option.series[1].data.concat(result.monitorMem);
					monitorChart.setOption({series: option.series});
					//monitorChart.setOption({title: {text: result[0]}});

				}
				// else {
				// 	//connect to new ws url
				// 	console.log(msg.data);
				// 	var idx = msg.data.indexOf("new url:")
				// 	if(idx >= 0) {
				// 		var newUrl = msg.data.substring(8).split(' ')[0];
				// 		newUrl = newUrl + '/'+ base_url
				// 		setTimeout(()=>{
				// 			socket.close();
				// 		},1000);
				//
				// 		setTimeout(()=>{
				// 			monitorChartRefresh(newUrl)
				// 		},2000);
				// 	}
				//
				// }
			};
			socket.onclose = function () {
				console.log("Socket closed");
			};
			//error
			socket.onerror = function () {
				alert("Socket error");
			}


		}
	}
	window.onbeforeunload = function () {
		socket.close();
	}
	//when window close, close socket
	window.unload = function () {
		socket.close();
	};

});
